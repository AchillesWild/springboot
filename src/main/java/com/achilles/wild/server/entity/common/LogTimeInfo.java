package com.achilles.wild.server.entity.common;

import com.achilles.wild.server.entity.BaseEntity;

public class LogTimeInfo extends BaseEntity {

    private String uri;

    private String type;

    private Integer layer;

    private String clz;

    private String method;

    private String params;

    private Integer time;

    private String traceId;

    public static void clear(LogTimeInfo logTimeInfo){
        if (logTimeInfo==null){
            return;
        }
        logTimeInfo.setId(null);
        logTimeInfo.setClz(null);
        logTimeInfo.setMethod(null);
        logTimeInfo.setParams(null);
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLayer() {
        return layer;
    }

    public void setLayer(Integer layer) {
        this.layer = layer;
    }

    public String getClz() {
        return clz;
    }

    public void setClz(String clz) {
        this.clz = clz;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
