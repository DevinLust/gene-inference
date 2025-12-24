package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.validation.ValidDistribution;

import java.util.Map;

public class SheepUpdateRequestDTO {
    private String name;

    // genotypes can be partial and phenotypes null here
    private Map<Category, SheepGenotypeDTO> genotypes;

    @ValidDistribution
    private Map<Category, Map<Grade, Double>> distributions;


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
}
