package com.progressengine.geneinference.dto;

public class NextStepRequest {

    private String runId;

    public NextStepRequest() {
    }

    public NextStepRequest(String runId) {
        this.runId = runId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }
}
