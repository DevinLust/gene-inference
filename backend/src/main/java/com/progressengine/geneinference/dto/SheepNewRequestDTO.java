package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public class SheepNewRequestDTO {
    private String name;
    private Map<Category, SheepGenotypeDTO> genotypes;
    private Map<Category, Map<Grade, Double>> distributions;
    private Integer parentRelationshipId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Category, SheepGenotypeDTO> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(Map<Category, SheepGenotypeDTO> genotypes) {
        this.genotypes = genotypes;
    }

    public Map<Category, Map<Grade, Double>> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<Category, Map<Grade, Double>> distributions) {
        this.distributions = distributions;
    }

    public Integer getParentRelationshipId() {
        return parentRelationshipId;
    }

    public void setParentRelationshipId(Integer parentRelationshipId) {
        this.parentRelationshipId = parentRelationshipId;
    }

    @Override
    public String toString() {
        return "SheepDTO{" +
                "name='" + name + '\'' +
                ", genotypes=" + genotypes +
                ", distributions=" + distributions +
                ", parentRelationshipId=" + parentRelationshipId +
                '}';
    }
}
