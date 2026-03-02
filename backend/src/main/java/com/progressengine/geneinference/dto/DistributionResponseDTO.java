package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public record DistributionResponseDTO(
        Category category,
        DistributionType type,
        Map<Integer, Map<Grade, Double>> distributions
) {}
