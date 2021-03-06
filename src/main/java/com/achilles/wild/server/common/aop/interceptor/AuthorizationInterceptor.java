package com.achilles.wild.server.common.aop.interceptor;

import com.achilles.wild.server.business.manager.user.UserTokenManager;
import com.achilles.wild.server.common.aop.exception.BizException;
import com.achilles.wild.server.common.constans.CommonConstant;
import com.achilles.wild.server.entity.user.UserToken;
import com.achilles.wild.server.model.response.code.UserResultCode;
import com.achilles.wild.server.tool.date.DateUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Controller
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Value("${if.verify.login:true}")
    private Boolean verifyLogin;

    @Autowired
    private UserTokenManager userTokenManager;

    private final Cache<String,Integer> keyCache = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
            .initialCapacity(5).maximumSize(1000).build();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!verifyLogin){
            return true;
        }

        String servletPath = request.getServletPath();
        log.debug("--------------------------servletPath : {}",servletPath);

        // 404
        if (handler instanceof ResourceHttpRequestHandler) {
            log.debug("----------404--------handler : " + handler);
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        NoCheckToken noCheckToken = method.getMethodAnnotation(NoCheckToken.class);
        if (noCheckToken !=null && noCheckToken.value()){
            return true;
        }

        String token = request.getHeader(CommonConstant.TOKEN);
        log.debug("------------------token:" + token);

        if(StringUtils.isBlank(token)){
            throw new BizException(UserResultCode.NOT_LOGIN);
        }
        Integer value = keyCache.getIfPresent(token);
        if (value != null) {
            return true;
        }
        UserToken userToken = userTokenManager.getByToken(token);
        if(userToken == null){
            throw new BizException(UserResultCode.NOT_LOGIN);
        }

        if(DateUtil.getCurrentDate().after(userToken.getExpirationTime())){
            throw new BizException(UserResultCode.LOGIN_EXPIRED);
        }

        keyCache.put(userToken.getToken(),1);

        UserToken userTokenUpdate = new UserToken();
        userTokenUpdate.setId(userToken.getId());
        userTokenUpdate.setExpirationTime(DateUtil.getDateByAddMill(CommonConstant.TOKEN_EXPIRATION_TIME));
        Boolean update = userTokenManager.updateById(userTokenUpdate);
        if(!update){
            throw new Exception("update token time fail");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        int status = response.getStatus();
        log.debug("-------------------------status : {}",status);


//        if(status == 404){
//            modelAndView.setViewName("/error/404");
//        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}
