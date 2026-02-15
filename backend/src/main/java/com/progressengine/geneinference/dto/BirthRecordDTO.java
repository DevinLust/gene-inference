package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.PhenotypeAtBirth;
import com.progressengine.geneinference.model.enums.Category;

import java.util.Map;

public class BirthRecordDTO {
    private Integer id;
    private Integer parentRelationshipId;
    private Integer childId;
    private Map<Category, PhenotypeAtBirth> phenotypesAtBirth;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentRelationshipId() {
        return parentRelationshipId;
    }

    public void setParentRelationshipId(Integer parentRelationshipId) {
        this.parentRelationshipId = parentRelationshipId;
    }

    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    public Map<Category, PhenotypeAtBirth> getPhenotypesAtBirth() {
        return phenotypesAtBirth;
    }

    public void setPhenotypesAtBirth(Map<Category, PhenotypeAtBirth> phenotypesAtBirth) {
        this.phenotypesAtBirth = phenotypesAtBirth;
    }
}
