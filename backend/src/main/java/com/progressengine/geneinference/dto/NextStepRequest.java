package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;

public class NextStepRequest {

    private String runId;
    private Category category;

    public NextStepRequest() {
    }

    public NextStepRequest(String runId, Category category) {
        this.runId = runId;
        this.category = category;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
