package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Grade;

public record SheepDistributionRow(
        Integer sheepId,
        Grade grade,
        double probability
) {
}
