package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Color;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ColorAlleleDomainTest {

    private final ColorAlleleDomain domain = new ColorAlleleDomain();

    // ----------------------------
    // expressionBias tests
    // ----------------------------

    @Test
    void expressionBias_sameAllele_isFiftyFifty() {
        double[] bias = domain.expressionBias(Color.RED, Color.RED);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
    }

    @Test
    void expressionBias_twoDominantAlleles_isFiftyFifty() {
        double[] bias = domain.expressionBias(Color.RED, Color.BLUE);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
    }

    @Test
    void expressionBias_recessiveVsDominant_dominantAlwaysWins_firstPosition() {
        double[] bias = domain.expressionBias(Color.RED, Color.NORMAL);

        assertEquals(1.0, bias[0], 1e-9);
        assertEquals(0.0, bias[1], 1e-9);
    }

    @Test
    void expressionBias_recessiveVsDominant_dominantAlwaysWins_secondPosition() {
        double[] bias = domain.expressionBias(Color.NORMAL, Color.RED);

        assertEquals(0.0, bias[0], 1e-9);
        assertEquals(1.0, bias[1], 1e-9);
    }

    @Test
    void expressionBias_recessiveWithRecessive_isFiftyFifty() {
        double[] bias = domain.expressionBias(Color.NORMAL, Color.NORMAL);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
    }

    // ----------------------------
    // sampling tests
    // ----------------------------

    @Test
    void sampleExpressionOrder_preservesInputAlleles() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Color> pair = domain.sampleExpressionOrder(Color.RED, Color.BLUE, random);

            Set<Color> alleles = EnumSet.of(pair.getFirst(), pair.getSecond());
            assertEquals(EnumSet.of(Color.RED, Color.BLUE), alleles);
        }
    }

    @Test
    void sampleExpressionOrder_dominantAlwaysExpressed_whenPairedWithRecessive() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Color> pair = domain.sampleExpressionOrder(Color.RED, Color.NORMAL, random);

            assertEquals(Color.RED, pair.getFirst(), "dominant allele should always be expressed");
            assertEquals(Color.NORMAL, pair.getSecond());
        }
    }

    @Test
    void sampleExpressionOrder_twoDominantAlleles_isApproximatelyFiftyFifty() {
        Random random = new Random(12345);
        int trials = 10_000;
        int firstExpressedCount = 0;

        for (int i = 0; i < trials; i++) {
            AllelePair<Color> pair = domain.sampleExpressionOrder(Color.RED, Color.BLUE, random);
            if (pair.getFirst() == Color.RED) {
                firstExpressedCount++;
            }
        }

        double observed = (double) firstExpressedCount / trials;
        assertEquals(0.5, observed, 0.03);
    }

    @Test
    void sampleExpressionOrder_sameAllele_alwaysSameOrder() {
        Random random = new Random(123);

        for (int i = 0; i < 100; i++) {
            AllelePair<Color> pair = domain.sampleExpressionOrder(Color.RED, Color.RED, random);

            assertEquals(Color.RED, pair.getFirst());
            assertEquals(Color.RED, pair.getSecond());
        }
    }
}
