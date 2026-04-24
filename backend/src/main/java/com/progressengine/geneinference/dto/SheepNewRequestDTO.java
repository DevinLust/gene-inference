package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.validation.ValidDistribution;
import com.progressengine.geneinference.validation.ValidGenotypes;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class SheepNewRequestDTO {
    private String name;

    @NotNull
    @ValidGenotypes
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

    @Override
    public String toString() {
        return "SheepDTO{" +
                "name='" + name + '\'' +
                ", genotypes=" + genotypes +
                '}';
    }
}
