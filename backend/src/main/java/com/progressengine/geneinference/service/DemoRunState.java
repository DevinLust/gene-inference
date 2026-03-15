package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.enums.RunStage;

import java.time.Instant;
import java.util.List;

public class DemoRunState {

    private final String runId;
    private final String userId;
    private final List<DemoStep> steps;

    private int currentStepIndex;
    private boolean completed;
    private Instant lastTouchedAt;

    public DemoRunState(String runId, String userId, List<DemoStep> steps) {
        this.runId = runId;
        this.userId = userId;
        this.steps = steps;
        this.currentStepIndex = 0;
        this.completed = false;
        this.lastTouchedAt = Instant.now();
    }

    public String getRunId() {
        return runId;
    }

    public String getUserId() {
        return userId;
    }

    public List<DemoStep> getSteps() {
        return steps;
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStepIndex(int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Instant getLastTouchedAt() {
        return lastTouchedAt;
    }

    public void touch() {
        this.lastTouchedAt = Instant.now();
    }

    public int getTotalSteps() {
        return steps.size();
    }

    public RunStage getCurrentStage() {
        if (completed) {
            return RunStage.COMPLETED;
        }
        if (steps.isEmpty()) {
            return RunStage.COMPLETED;
        }
        if (currentStepIndex <= 0) {
            return steps.get(0).getStage();
        }
        int idx = Math.min(currentStepIndex - 1, steps.size() - 1);
        return steps.get(idx).getStage();
    }
}
