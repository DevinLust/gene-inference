package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;

import java.util.Map;

public class SheepResponseDTO {
    private Integer id;
    private String name;
    private Map<Category, SheepGenotypeDTO> genotypes;
    private Map<Category, Map<DistributionType, Map<String, Double>>> distributions;
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

    public Map<Category, SheepGenotypeDTO> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(Map<Category, SheepGenotypeDTO> genotypes) {
        this.genotypes = genotypes;
    }

    public Map<Category, Map<DistributionType, Map<String, Double>>> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<Category, Map<DistributionType, Map<String, Double>>> distributions) {
        this.distributions = distributions;
    }

    public Integer getParentRelationshipId() {
        return parentRelationshipId;
    }

    public void setParentRelationshipId(Integer parentRelationshipId) {
        this.parentRelationshipId = parentRelationshipId;
    }
}
