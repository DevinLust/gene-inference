package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.RunStage;

public class StepEventPayload {

    private int stepIndex;
    private int totalSteps;
    private RunStage stage;
    private String message;

    public StepEventPayload() {
    }

    public StepEventPayload(int stepIndex, int totalSteps, RunStage stage, String message) {
        this.stepIndex = stepIndex;
        this.totalSteps = totalSteps;
        this.stage = stage;
        this.message = message;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public RunStage getStage() {
        return stage;
    }

    public void setStage(RunStage stage) {
        this.stage = stage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
