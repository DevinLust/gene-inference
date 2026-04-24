package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;

import java.util.Map;

public record PredictionResponseDTO(Map<Category, Map<String, Double>> phenotypeDistributions) {
}
