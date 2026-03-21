package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.RunStage;

public class RunStartedPayload {

    private int totalSteps;
    private int currentStep;
    private RunStage stage;
    private VisualGraphSnapshot graph;

    public RunStartedPayload() {
    }

    public RunStartedPayload(int totalSteps, int currentStep, RunStage stage, VisualGraphSnapshot graph) {
        this.totalSteps = totalSteps;
        this.currentStep = currentStep;
        this.stage = stage;
        this.graph = graph;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public RunStage getStage() {
        return stage;
    }

    public void setStage(RunStage stage) {
        this.stage = stage;
    }

    public VisualGraphSnapshot getGraph() {
        return graph;
    }

    public void setGraph(VisualGraphSnapshot graph) {
        this.graph = graph;
    }
}
