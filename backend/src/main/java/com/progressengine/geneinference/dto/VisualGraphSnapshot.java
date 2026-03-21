package com.progressengine.geneinference.dto;

import java.util.List;

public class VisualGraphSnapshot {
    private String centerSheepId;
    private List<VisualNodeDTO> nodes;
    private List<VisualEdgeDTO> edges;

    public VisualGraphSnapshot() {}

    public VisualGraphSnapshot(String centerSheepId, List<VisualNodeDTO> nodes, List<VisualEdgeDTO> edges) {
        this.centerSheepId = centerSheepId;
        this.nodes = nodes;
        this.edges = edges;
    }

    public String getCenterSheepId() { return centerSheepId; }
    public void setCenterSheepId(String centerSheepId) { this.centerSheepId = centerSheepId; }

    public List<VisualNodeDTO> getNodes() { return nodes; }
    public void setNodes(List<VisualNodeDTO> nodes) { this.nodes = nodes; }

    public List<VisualEdgeDTO> getEdges() { return edges; }
    public void setEdges(List<VisualEdgeDTO> edges) { this.edges = edges; }
}
