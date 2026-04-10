package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;

import java.util.Set;

public record ExcessAlleleViolation(
        Category category,
        String attemptedAllele,
        Set<String> validAlleles
) {}
