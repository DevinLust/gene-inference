package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;

import java.util.Map;

public class SheepUpdateRequestDTO {
    private String name;

    // genotypes can be partial and phenotypes null here
    private Map<Category, SheepGenotypeDTO> genotypes;


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
}
