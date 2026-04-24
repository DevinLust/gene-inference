package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;

import java.util.Map;

public record DistributionResponseDTO(
        Category category,
        DistributionType type,
        Map<Integer, Map<String, Double>> distributions
) {}
