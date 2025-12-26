package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public record PredictionResponseDTO(Map<Category, Map<Grade, Double>> phenotypeDistributions) {
}
