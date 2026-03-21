package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.RelationshipEdgeRole;

public class VisualEdgeDTO {
    private String id;
    private String sourceId;
    private String targetId;
    private String type; // full or stub
    private boolean visibleTarget;
    private RelationshipEdgeRole relationshipRole;

    private Double stubAngleRadians;
    private Integer stubIndex;
    private Integer stubCount;

    public VisualEdgeDTO() {
    }

    public VisualEdgeDTO(
            String id,
            String sourceId,
            String targetId,
            String type,
            boolean visibleTarget,
            RelationshipEdgeRole relationshipRole,
            Double stubAngleRadians,
            Integer stubIndex,
            Integer stubCount
    ) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.visibleTarget = visibleTarget;
        this.relationshipRole = relationshipRole;
        this.stubAngleRadians = stubAngleRadians;
        this.stubIndex = stubIndex;
        this.stubCount = stubCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVisibleTarget() {
        return visibleTarget;
    }

    public void setVisibleTarget(boolean visibleTarget) {
        this.visibleTarget = visibleTarget;
    }

    public RelationshipEdgeRole getRelationshipRole() {
        return relationshipRole;
    }

    public void setRelationshipRole(RelationshipEdgeRole relationshipRole) {
        this.relationshipRole = relationshipRole;
    }

    public Double getStubAngleRadians() {
        return stubAngleRadians;
    }

    public void setStubAngleRadians(Double stubAngleRadians) {
        this.stubAngleRadians = stubAngleRadians;
    }

    public Integer getStubIndex() {
        return stubIndex;
    }

    public void setStubIndex(Integer stubIndex) {
        this.stubIndex = stubIndex;
    }

    public Integer getStubCount() {
        return stubCount;
    }

    public void setStubCount(Integer stubCount) {
        this.stubCount = stubCount;
    }
}
