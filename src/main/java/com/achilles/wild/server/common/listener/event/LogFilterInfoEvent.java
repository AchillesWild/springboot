package com.achilles.wild.server.common.listener.event;

import org.springframework.context.ApplicationEvent;

public class LogFilterInfoEvent extends ApplicationEvent {

    public LogFilterInfoEvent(Object source) {
        super(source);
    }
}
