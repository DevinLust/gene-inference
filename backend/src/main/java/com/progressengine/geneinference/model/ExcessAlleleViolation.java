package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Set;

public record ExcessAlleleViolation(
        Category category,
        Grade attemptedAllele,
        Set<Grade> validAlleles
) {}
