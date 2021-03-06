package com.achilles.wild.server.common.aop.log;

import com.achilles.wild.server.common.aop.exception.BizException;
import com.achilles.wild.server.common.config.params.LogBizParamsConfig;
import com.achilles.wild.server.common.constans.CommonConstant;
import com.achilles.wild.server.common.listener.event.LogExceptionInfoEvent;
import com.achilles.wild.server.entity.common.LogExceptionInfo;
import com.achilles.wild.server.entity.common.LogTimeInfo;
import com.achilles.wild.server.enums.account.ExceptionTypeEnum;
import com.achilles.wild.server.tool.bean.AspectUtil;
import com.achilles.wild.server.tool.date.DateUtil;
import com.achilles.wild.server.tool.json.JsonUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import com.lmax.disruptor.RingBuffer;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Aspect
@Component
@Order(0)
public class LogControllerAspect {

    private final static Logger log = LoggerFactory.getLogger(LogControllerAspect.class);

    private final static String PREFIX = "";

    private Cache<String,AtomicInteger> integerCache = CacheBuilder.newBuilder().concurrencyLevel(5000).maximumSize(500).expireAfterWrite(10, TimeUnit.SECONDS).build();

    private Cache<String, RateLimiter> rateLimiterCache = CacheBuilder.newBuilder().concurrencyLevel(5000).maximumSize(500).expireAfterWrite(10, TimeUnit.SECONDS).build();

    @Autowired
    private LogBizParamsConfig logBizParamsConfig;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    Queue<LogTimeInfo> logInfoConcurrentLinkedQueue;

    @Autowired
    private RingBuffer<LogTimeInfo> messageModelRingBuffer;

    @Pointcut("execution(* com.achilles.wild.server.business.controller..*.*(..))")
    public void controllerLog() {}


    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {

//        if(!openLog){
//            return;
//        }
//        String method = joinPoint.getSignature().getDeclaringTypeName()+"#"+joinPoint.getSignature().getName();
//        Map<String,Object> paramsMap =  getParamsMap(joinPoint);
//        log.info(PREFIX +"#params : "+method+"("+paramsMap+")");
    }


    @Around("controllerLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        if(!logBizParamsConfig.getIfOpenLog()){
            return proceedingJoinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        String clz = methodSignature.getDeclaringTypeName();
        String method = methodSignature.getName();
        Map<String,Object> paramsMap = AspectUtil.getParamsMap(proceedingJoinPoint);
        String params = null;
        if(paramsMap.size()!=0){
            params = JsonUtil.toJsonString(paramsMap);
        }

        Object result = proceedingJoinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;
        String path = clz+"#"+method;
        log.debug(PREFIX +"#result : "+path+"-->"+ JsonUtil.toJsonString(result));
        log.debug(PREFIX +"#time-consuming : "+path+"-->("+duration+"ms)");
        if(!logBizParamsConfig.getIfTimeLogInsertDb()){
            return result;
        }

//        if(!controllerLogParamsConfig.getIfTimeLogInsertDb() || duration<=controllerLogParamsConfig.getTimeLimit()){
//            return result;
//        }
//
//        String countLimitKey = path+"_CountLimit";
//        AtomicInteger atomicInteger = integerCache.getIfPresent(countLimitKey)==null?new AtomicInteger():integerCache.getIfPresent(countLimitKey);
//        int count = atomicInteger.get();
//        if(count>=controllerLogParamsConfig.getCountOfInsertDBInTime()){
//            return result;
//        }
//        count = atomicInteger.incrementAndGet();
//        if(count>controllerLogParamsConfig.getCountOfInsertDBInTime()){
//            return result;
//        }
//        integerCache.put(countLimitKey,atomicInteger);
//
//        String rateLimiterKey = path+"_RateLimiter";
//        RateLimiter rateLimiter = rateLimiterCache.getIfPresent(rateLimiterKey)==null ?
//                                  RateLimiter.create(controllerLogParamsConfig.getRateOfInsertDBPerSecond()):rateLimiterCache.getIfPresent(rateLimiterKey);
//        rateLimiterCache.put(rateLimiterKey,rateLimiter);
//        if(!rateLimiter.tryAcquire()){
//            return result;
//        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String servletPath = request.getServletPath();
        String type = request.getMethod();

        log.debug(PREFIX +"#insert slow log into db start, method : "+path+"-->"+ params+""+"--->"+duration+"ms");
        long sequence = messageModelRingBuffer.next();
        LogTimeInfo logTimeInfo = messageModelRingBuffer.get(sequence);
        LogTimeInfo.clear(logTimeInfo);
        logTimeInfo.setUri(servletPath);
        logTimeInfo.setType(type);
        logTimeInfo.setLayer(1);
        logTimeInfo.setClz(clz);
        logTimeInfo.setMethod(method);
        logTimeInfo.setParams(params);
        logTimeInfo.setTime((int)duration);
        logTimeInfo.setTraceId(MDC.get(CommonConstant.TRACE_ID));
        logTimeInfo.setCreateDate(DateUtil.getCurrentDate());
        logTimeInfo.setUpdateDate(logTimeInfo.getCreateDate());
//        boolean add = logInfoConcurrentLinkedQueue.offer(logTimeInfo);
//        log.debug(PREFIX +"#---------controller add to queue success : "+add);

//        messageModelRingBuffer.publish(sequence);

        return result;
    }

    @AfterThrowing(pointcut="controllerLog()", throwing= "throwable")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable){

        if(!logBizParamsConfig.getIfExceptionLogInsertDb()){
            log.debug("insert into DB  BizException has been closed");
           return;
        }

        String clz = joinPoint.getSignature().getDeclaringTypeName();
        String method = joinPoint.getSignature().getName();
        Map<String,Object> paramsMap = AspectUtil.getParamsMap(joinPoint);
        String params = null;
        if(paramsMap.size()!=0){
            params = JsonUtil.toJsonString(paramsMap);
        }

        LogExceptionInfo logExceptionInfo = new LogExceptionInfo();
        logExceptionInfo.setClz(clz);
        logExceptionInfo.setMethod(method);
        logExceptionInfo.setParams(params);
        logExceptionInfo.setTraceId(MDC.get(CommonConstant.TRACE_ID));

        if(throwable instanceof BizException){
            log.debug("----------------------insert into DB  BizException ");
            logExceptionInfo.setMessage(throwable.getMessage());
            logExceptionInfo.setType(ExceptionTypeEnum.BIZ_EXCEPTION.toNumbericValue());
        }else {
            log.debug("----------------------insert into DB other Exception ");
            logExceptionInfo.setMessage(throwable.toString());
            logExceptionInfo.setType(ExceptionTypeEnum.OTHER_EXCEPTION.toNumbericValue());
        }
        if (StringUtils.isEmpty(logExceptionInfo.getMessage())) {
            logExceptionInfo.setMessage("0");
        }
//        eventListeners.addExceptionLogsEvent(new ExceptionLogsEvent(exceptionLogs));
        applicationContext.publishEvent(new LogExceptionInfoEvent(logExceptionInfo));

    }

    @After("controllerLog()")
    public void doAfter() throws Throwable {

    }

}
