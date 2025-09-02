package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public class PredictionResponseDTO {
    private final Map<Category, Map<Grade, Double>> phenotypeDistributions;

    public PredictionResponseDTO(Map<Category, Map<Grade, Double>> phenotypeDistributions) {
        this.phenotypeDistributions = phenotypeDistributions;
    }

    public Map<Category, Map<Grade, Double>> getPhenotypeDistributions() {
        return phenotypeDistributions;
    }
}
