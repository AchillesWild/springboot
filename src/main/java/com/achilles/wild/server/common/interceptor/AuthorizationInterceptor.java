package com.achilles.wild.server.common.interceptor;

import com.achilles.wild.server.common.constans.CommonConstant;
import com.achilles.wild.server.common.exception.MyException;
import com.achilles.wild.server.model.response.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Value("${if.verify.login}")
    private Boolean verifyLogin;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!verifyLogin){
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        NoLogin noLogin = method.getMethodAnnotation(NoLogin.class);
        if (noLogin!=null && noLogin.value()){
            return true;
        }

        String token = request.getHeader(CommonConstant.TOKEN);
        log.info("------------------------------------token:"+ token);

        if(StringUtils.isBlank(token)){
            throw new MyException(ResultCode.NOT_LOGIN);
        }

        //todo
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}