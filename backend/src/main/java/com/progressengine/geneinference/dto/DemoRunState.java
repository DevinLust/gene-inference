package com.progressengine.geneinference.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DemoRunState {
    String runId;
    String userId;
    int currentStepIndex;
    List<String> steps;
    boolean completed;

    public DemoRunState(String userId) {
        this.userId = userId;
        this.runId = UUID.randomUUID().toString();
        this.currentStepIndex = 0;
        this.steps = new ArrayList<>();
        this.completed = false;
    }

    public DemoRunState(String runId, String userId) {
        this.runId = runId;
        this.userId = userId;
        this.currentStepIndex = 0;
        this.steps = new ArrayList<>();
        this.completed = false;
    }
}
