package com.progressengine.geneinference.dto;

import java.util.Map;

public class RunEvent {
    private String type;          // stage_changed, propagation_batch, belief_updated, completed
    private String runId;
    private Object payload;

    public RunEvent() {}

    public RunEvent(String type, String runId, Object payload) {
        this.type = type;
        this.runId = runId;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
