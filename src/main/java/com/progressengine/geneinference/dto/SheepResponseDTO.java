package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public class SheepResponseDTO {
    private Integer id;
    private String name;
    private Grade phenotype;
    private Grade hiddenAllele;
    private Map<Grade, Double> hiddenDistribution;
    private Integer parentRelationshipId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getHiddenAllele() {
        return hiddenAllele;
    }

    public void setHiddenAllele(Grade hiddenAllele) {
        this.hiddenAllele = hiddenAllele;
    }

    public Grade getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(Grade phenotype) {
        this.phenotype = phenotype;
    }

    public Map<Grade, Double> getHiddenDistribution() {
        return hiddenDistribution;
    }

    public void setHiddenDistribution(Map<Grade, Double> hiddenDistribution) {
        this.hiddenDistribution = hiddenDistribution;
    }

    public Integer getParentRelationshipId() {
        return parentRelationshipId;
    }

    public void setParentRelationshipId(Integer parentRelationshipId) {
        this.parentRelationshipId = parentRelationshipId;
    }
}
