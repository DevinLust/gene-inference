package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.GeneticViolationReason;

import java.util.Set;

public record ExcessAlleleViolation(
        Category category,
        String attemptedAllele,
        Set<String> validAlleles,
        GeneticViolationReason reason,
        String message
) {}
