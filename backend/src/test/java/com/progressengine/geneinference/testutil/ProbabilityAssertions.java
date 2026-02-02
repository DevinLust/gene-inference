package com.progressengine.geneinference.testutil;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class ProbabilityAssertions {

    private ProbabilityAssertions() {} // prevent instantiation

    public static <T> void assertValidDistribution(
            Map<T, Double> distribution
    ) {
        double sum = distribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        assertAll(
                () -> assertEquals(1.0, sum, 1e-9, "Sum of distribution values is not 1.0: " + sum),
                () -> distribution.forEach((key, value) -> assertTrue(value >= 0.0 && value <= 1.0, "Value for key " + key.toString() + " is not between 0 and 1: " + value))
        );
    }

    public static <T> void assertUniform(
            Map<T, Double> map,
            double epsilon
    ) {
        if (map.isEmpty()) {
            throw new AssertionError("Distribution is empty");
        }

        double first = map.values().iterator().next();

        assertTrue(
                map.values().stream()
                        .allMatch(v -> Math.abs(v - first) < epsilon),
                "Values are not uniform: " + map
        );
    }
}

