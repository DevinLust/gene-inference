package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.PhenotypeAtBirth;
import com.progressengine.geneinference.model.enums.Category;

import java.util.Map;

public class BirthRecordDTO {
    private Integer id;
    private RelationshipSummaryResponseDTO parentRelationship;
    private SheepSummaryResponseDTO child;
    private Map<Category, PhenotypeAtBirth> phenotypesAtBirth;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RelationshipSummaryResponseDTO getParentRelationshipSummary() {
        return parentRelationship;
    }

    public void setParentRelationship(RelationshipSummaryResponseDTO parentRelationship) {
        this.parentRelationship = parentRelationship;
    }

    public SheepSummaryResponseDTO getChild() {
        return child;
    }

    public void setChild(SheepSummaryResponseDTO child) {
        this.child = child;
    }

    public Map<Category, PhenotypeAtBirth> getPhenotypesAtBirth() {
        return phenotypesAtBirth;
    }

    public void setPhenotypesAtBirth(Map<Category, PhenotypeAtBirth> phenotypesAtBirth) {
        this.phenotypesAtBirth = phenotypesAtBirth;
    }
}
