package com.progressengine.geneinference.dto;

public record SheepDistributionRow(
        Integer sheepId,
        String allele,
        double probability
) {
}
