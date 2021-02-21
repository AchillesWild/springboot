package com.achilles.wild.server.common.aop.interceptor;

import com.achilles.wild.server.entity.user.TokenRecord;
import com.achilles.wild.server.business.manager.user.TokenRecordManager;
import com.achilles.wild.server.common.aop.exception.BizException;
import com.achilles.wild.server.common.constans.CommonConstant;
import com.achilles.wild.server.model.response.code.BaseResultCode;
import com.achilles.wild.server.model.response.code.UserResultCode;
import com.achilles.wild.server.tool.date.DateUtil;
import com.achilles.wild.server.tool.generate.unique.GenerateUniqueUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final static Logger log = LoggerFactory.getLogger(AuthorizationInterceptor.class);

    @Value("${if.verify.login:false}")
    private Boolean verifyLogin;

    @Value("${if.verify.trace.id:false}")
    private Boolean verifyTraceId;

    @Autowired
    private TokenRecordManager tokenRecordManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String traceId = request.getHeader(CommonConstant.TRACE_ID);
        if(verifyTraceId){
            log.debug("---------------traceId  from  client---------------------:" + traceId);
            checkTraceId(traceId);
        }else{
            traceId = DateUtil.getCurrentStr(DateUtil.YYYY_MM_DD_HH_MM_SS_SSS)+"_"
                    + CommonConstant.SYSTEM_CODE+"_"
                    + GenerateUniqueUtil.getUuId();
            log.debug("---------------traceId  generate by  system---------------------:" + traceId);
        }

        MDC.put(CommonConstant.TRACE_ID,traceId);

        if(!verifyLogin){
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        NoCheckToken noCheckToken = method.getMethodAnnotation(NoCheckToken.class);
        if (noCheckToken !=null && noCheckToken.value()){
            return true;
        }

        String token = request.getHeader(CommonConstant.TOKEN);
        log.debug("------------------------------------token:"+ token);

        if(StringUtils.isBlank(token)){
            throw new BizException(UserResultCode.NOT_LOGIN.code,UserResultCode.NOT_LOGIN.message);
        }

        TokenRecord tokenRecord = tokenRecordManager.getByToken(token);
        if(tokenRecord==null){
            throw new BizException(UserResultCode.NOT_LOGIN.code,UserResultCode.NOT_LOGIN.message);
        }
        int seconds = DateUtil.getGapSeconds(tokenRecord.getUpdateDate());
        if(seconds>1800){
            throw new BizException(UserResultCode.LOGIN_EXPIRED.code,UserResultCode.LOGIN_EXPIRED.message);
        }

        TokenRecord tokenRecordUpdate = new TokenRecord();
        tokenRecordUpdate.setId(tokenRecord.getId());
        Boolean update = tokenRecordManager.updateById(tokenRecord);
        if(!update){
            throw new Exception("update token time fail");
        }

        return true;
    }

    private void checkTraceId(String traceId) {
        if(StringUtils.isBlank(traceId)){
           throw new BizException(BaseResultCode.TRACE_ID_NECESSARY.code,BaseResultCode.TRACE_ID_NECESSARY.message);
       }
        if (traceId.length()<20 || traceId.length()>64){
            throw new BizException(BaseResultCode.TRACE_ID_LENGTH_ILLEGAL.code,BaseResultCode.TRACE_ID_LENGTH_ILLEGAL.message);
        }
        String prefix = traceId.substring(0,17);
        Date submitDate = null;
        try {
            submitDate = DateUtil.getDateFormat(DateUtil.YYYY_MM_DD_HH_MM_SS_SSS,prefix);
        } catch (BizException e) {
            throw new BizException(BaseResultCode.TRACE_ID_PREFIX_ILLEGAL.code,BaseResultCode.TRACE_ID_PREFIX_ILLEGAL.message);
        }
        if (submitDate==null){
            throw new BizException(BaseResultCode.TRACE_ID_PREFIX_ILLEGAL.code,BaseResultCode.TRACE_ID_PREFIX_ILLEGAL.message);
        }

        int seconds = DateUtil.getGapSeconds(submitDate);
        if(seconds>30){
            throw new BizException(BaseResultCode.TRACE_ID_CONTENT_EXPIRED.code,BaseResultCode.TRACE_ID_CONTENT_EXPIRED.message);
        }

        if(seconds<-5){
            throw new BizException(BaseResultCode.TRACE_ID_CONTENT_EXCEED_CURRENT.code,BaseResultCode.TRACE_ID_CONTENT_EXCEED_CURRENT.message);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        log.debug("-----------------remove traceId from Thread-----");
        MDC.remove(CommonConstant.TRACE_ID);
    }

}
