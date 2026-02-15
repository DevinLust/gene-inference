package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.validation.ValidDistribution;
import com.progressengine.geneinference.validation.ValidGenotypes;
import com.progressengine.geneinference.validation.ValidParents;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Map;

@ValidParents
public class SheepBreedRequestDTO {
    private String name;

    @NotNull
    @ValidGenotypes
    private Map<Category, SheepGenotypeDTO> genotypes;

    @ValidDistribution
    private Map<Category, Map<Grade, Double>> distributions;

    @Positive(message = "parent1Id must be positive if present")
    private Integer parent1Id;

    @Positive(message = "parent2Id must be positive if present")
    private Integer parent2Id;


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

    public Integer getParent1Id() {
        return parent1Id;
    }

    public void setParent1Id(Integer parent1Id) {
        this.parent1Id = parent1Id;
    }

    public Integer getParent2Id() {
        return parent2Id;
    }

    public void setParent2Id(Integer parent2Id) {
        this.parent2Id = parent2Id;
    }

    @Override
    public String toString() {
        return "SheepDTO{" +
                "name='" + name + '\'' +
                ", genotypes=" + genotypes +
                ", distributions=" + distributions +
                ", parent1Id=" + parent1Id +
                ", parent2Id=" + parent2Id +
                '}';
    }
}

