package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Tone;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ToneAlleleDomainTest {

    private final ToneAlleleDomain domain = new ToneAlleleDomain();

    @Test
    void expressionBias_sameAllele_isFiftyFifty() {
        double[] bias = domain.expressionBias(Tone.MONOTONE, Tone.MONOTONE);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_differentAlleles_isFiftyFifty() {
        double[] bias = domain.expressionBias(Tone.MONOTONE, Tone.TWO_TONE);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_isSymmetric() {
        double[] forward = domain.expressionBias(Tone.MONOTONE, Tone.TWO_TONE);
        double[] reverse = domain.expressionBias(Tone.TWO_TONE, Tone.MONOTONE);

        assertEquals(forward[0], reverse[1], 1e-9);
        assertEquals(forward[1], reverse[0], 1e-9);
    }

    @Test
    void sampleExpressionOrder_sameAllele_alwaysReturnsSamePair() {
        Random random = new Random(123);

        for (int i = 0; i < 100; i++) {
            AllelePair<Tone> pair = domain.sampleExpressionOrder(Tone.MONOTONE, Tone.MONOTONE, random);

            assertEquals(Tone.MONOTONE, pair.getFirst());
            assertEquals(Tone.MONOTONE, pair.getSecond());
        }
    }

    @Test
    void sampleExpressionOrder_preservesInputAlleles() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Tone> pair = domain.sampleExpressionOrder(Tone.MONOTONE, Tone.TWO_TONE, random);

            Set<Tone> alleles = EnumSet.of(pair.getFirst(), pair.getSecond());
            assertEquals(EnumSet.of(Tone.MONOTONE, Tone.TWO_TONE), alleles);
        }
    }

    @Test
    void sampleExpressionOrder_isApproximatelyFiftyFifty() {
        Random random = new Random(12345);
        int trials = 10_000;
        int firstExpressedCount = 0;

        for (int i = 0; i < trials; i++) {
            AllelePair<Tone> pair = domain.sampleExpressionOrder(Tone.MONOTONE, Tone.TWO_TONE, random);
            if (pair.getFirst() == Tone.MONOTONE) {
                firstExpressedCount++;
            }
        }

        double observed = (double) firstExpressedCount / trials;
        assertEquals(0.5, observed, 0.03);
    }
}
