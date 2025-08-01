package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public class SheepRequestDTO {
    private Integer id;
    private String name;
    private Grade phenotype;
    private Grade hiddenAllele;
    private Map<Category, SheepGenotypeDTO> genotypes;
    private Map<Grade, Double> hiddenDistribution;
    private Map<Category, Map<Grade, Double>> distributions;
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

    public Map<Category, SheepGenotypeDTO> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(Map<Category, SheepGenotypeDTO> genotypes) {
        this.genotypes = genotypes;
    }

    public Map<Grade, Double> getHiddenDistribution() {
        return hiddenDistribution;
    }

    public void setHiddenDistribution(Map<Grade, Double> hiddenDistribution) {
        this.hiddenDistribution = hiddenDistribution;
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
}
