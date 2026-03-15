package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.enums.RunStage;

public class DemoStep {

    private final RunStage stage;
    private final String message;

    public DemoStep(RunStage stage, String message) {
        this.stage = stage;
        this.message = message;
    }

    public RunStage getStage() {
        return stage;
    }

    public String getMessage() {
        return message;
    }
}