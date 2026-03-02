package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

public record BirthRecordSearchParams(
        Integer relationshipId,
        Category category,
        Grade p1,
        Grade p2
) {

    public boolean hasAnyParentsAtBirth() {
        return category != null || p1 != null || p2 != null;
    }

    public boolean hasCompleteParentsAtBirth() {
        return category != null && p1 != null && p2 != null;
    }
}
