package com.progressengine.geneinference.dto;

import java.util.List;

public class MessageWaveDelta {
    private String waveType;
    private String category;
    private List<String> activeFullEdgeIds;
    private List<String> activeStubEdgeIds;

    public MessageWaveDelta() {
    }

    public MessageWaveDelta(
            String waveType,
            String category,
            List<String> activeFullEdgeIds,
            List<String> activeStubEdgeIds
    ) {
        this.waveType = waveType;
        this.category = category;
        this.activeFullEdgeIds = activeFullEdgeIds;
        this.activeStubEdgeIds = activeStubEdgeIds;
    }

    public String getWaveType() {
        return waveType;
    }

    public void setWaveType(String waveType) {
        this.waveType = waveType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getActiveFullEdgeIds() {
        return activeFullEdgeIds;
    }

    public void setActiveFullEdgeIds(List<String> activeFullEdgeIds) {
        this.activeFullEdgeIds = activeFullEdgeIds;
    }

    public List<String> getActiveStubEdgeIds() {
        return activeStubEdgeIds;
    }

    public void setActiveStubEdgeIds(List<String> activeStubEdgeIds) {
        this.activeStubEdgeIds = activeStubEdgeIds;
    }
}
