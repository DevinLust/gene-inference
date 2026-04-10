package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.validation.ValidDistribution;
import com.progressengine.geneinference.validation.ValidGenotypes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

public class SheepReplaceRequestDTO {
    private String name;

    @NotNull
    @ValidGenotypes
    private Map<Category, SheepGenotypeDTO> genotypes;

    @ValidDistribution
    private Map<Category, Map<String, Double>> distributions;

    @Positive(message = "parentRelationshipId must be positive if present")
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

    public Map<Category, Map<String, Double>> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<Category, Map<String, Double>> distributions) {
        this.distributions = distributions;
    }

    public Integer getParentRelationshipId() {
        return parentRelationshipId;
    }

    public void setParentRelationshipId(Integer parentRelationshipId) {
        this.parentRelationshipId = parentRelationshipId;
    }
}
