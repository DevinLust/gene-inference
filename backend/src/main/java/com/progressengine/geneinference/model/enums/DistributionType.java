package com.progressengine.geneinference.model.enums;

public enum DistributionType {
    PRIOR, INFERRED;

    public static DistributionType fromString(String str) {
        return DistributionType.valueOf(str); // Throws IllegalArgumentException if not valid
    }
}
