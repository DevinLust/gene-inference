package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.RunEventType;

public class RunEvent {

    private RunEventType type;
    private String runId;
    private Object payload;

    public RunEvent() {
    }

    public RunEvent(RunEventType type, String runId, Object payload) {
        this.type = type;
        this.runId = runId;
        this.payload = payload;
    }

    public RunEventType getType() {
        return type;
    }

    public void setType(RunEventType type) {
        this.type = type;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
