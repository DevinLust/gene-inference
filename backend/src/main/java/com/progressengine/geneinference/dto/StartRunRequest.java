package com.progressengine.geneinference.dto;

public class StartRunRequest {
    private String graphId;
    private String focusSheepId;

    public String getGraphId() { return graphId; }
    public void setGraphId(String graphId) { this.graphId = graphId; }

    public String getFocusSheepId() { return focusSheepId; }
    public void setFocusSheepId(String focusSheepId) { this.focusSheepId = focusSheepId; }
}
