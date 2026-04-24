package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GradeAlleleDomainTest {

    private final GradeAlleleDomain domain = new GradeAlleleDomain();

    @Test
    void expressionBias_sameAllele_isFiftyFifty() {
        double[] bias = domain.expressionBias(Grade.A, Grade.A);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_betterGradeFirst_isSeventyThirty() {
        double[] bias = domain.expressionBias(Grade.A, Grade.C);

        assertEquals(0.7, bias[0], 1e-9);
        assertEquals(0.3, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_betterGradeSecond_isThirtySeventy() {
        double[] bias = domain.expressionBias(Grade.C, Grade.A);

        assertEquals(0.3, bias[0], 1e-9);
        assertEquals(0.7, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void sampleExpressionOrder_sameAllele_alwaysReturnsSamePair() {
        Random random = new Random(123);

        for (int i = 0; i < 100; i++) {
            AllelePair<Grade> pair = domain.sampleExpressionOrder(Grade.B, Grade.B, random);

            assertEquals(Grade.B, pair.getFirst());
            assertEquals(Grade.B, pair.getSecond());
        }
    }

    @Test
    void sampleExpressionOrder_preservesInputAlleles() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Grade> pair = domain.sampleExpressionOrder(Grade.A, Grade.C, random);

            Set<Grade> alleles = EnumSet.of(pair.getFirst(), pair.getSecond());
            assertEquals(EnumSet.of(Grade.A, Grade.C), alleles);
        }
    }

    @Test
    void sampleExpressionOrder_betterGradeFirst_isApproximatelySeventyPercentExpressed() {
        Random random = new Random(12345);
        int trials = 10_000;
        int firstExpressedCount = 0;

        for (int i = 0; i < trials; i++) {
            AllelePair<Grade> pair = domain.sampleExpressionOrder(Grade.A, Grade.C, random);
            if (pair.getFirst() == Grade.A) {
                firstExpressedCount++;
            }
        }

        double observed = (double) firstExpressedCount / trials;
        assertEquals(0.7, observed, 0.03);
    }

    @Test
    void sampleExpressionOrder_betterGradeSecond_isApproximatelySeventyPercentExpressed() {
        Random random = new Random(12345);
        int trials = 10_000;
        int secondInputExpressedCount = 0;

        for (int i = 0; i < trials; i++) {
            AllelePair<Grade> pair = domain.sampleExpressionOrder(Grade.C, Grade.A, random);
            if (pair.getFirst() == Grade.A) {
                secondInputExpressedCount++;
            }
        }

        double observed = (double) secondInputExpressedCount / trials;
        assertEquals(0.7, observed, 0.03);
    }
}
