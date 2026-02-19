package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public record RelationshipResponseDTO(
        Integer id,
        SheepSummaryResponseDTO parent1,
        SheepSummaryResponseDTO parent2,
        Map<Category, Map<GradePair, Map<Grade, Integer>>> phenotypeFrequencies
) {}
