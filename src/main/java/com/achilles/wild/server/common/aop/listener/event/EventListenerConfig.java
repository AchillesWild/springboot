package com.achilles.wild.server.common.aop.listener.event;

import com.achilles.wild.server.tool.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EventListenerConfig {

    private final static Logger log = LoggerFactory.getLogger(EventListenerConfig.class);

    @Async
    @EventListener
    public void handleEvent(EventListenerConfigEvent event) {

//        try {
//            Thread.sleep(3000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        log.info("--------EventListenerConfigEvent--------------------"+ JsonUtil.toJsonString(event.getSource()));

    }
}