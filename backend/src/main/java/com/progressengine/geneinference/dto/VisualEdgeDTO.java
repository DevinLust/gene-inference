package com.progressengine.geneinference.dto;

public class VisualEdgeDTO {
    private String id;
    private String sourceId;
    private String targetId;
    private String type; // full, stub
    private boolean visibleTarget; // useful for stubs

    public VisualEdgeDTO() {}

    public VisualEdgeDTO(String id, String sourceId, String targetId, String type, boolean visibleTarget) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.visibleTarget = visibleTarget;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isVisibleTarget() { return visibleTarget; }
    public void setVisibleTarget(boolean visibleTarget) { this.visibleTarget = visibleTarget; }
}
