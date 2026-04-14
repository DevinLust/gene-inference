package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Shiny;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ShinyAlleleDomainTest {

    private final ShinyAlleleDomain domain = new ShinyAlleleDomain();

    @Test
    void expressionBias_sameShiny_isFiftyFifty() {
        double[] bias = domain.expressionBias(Shiny.SHINY, Shiny.SHINY);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_sameNonShiny_isFiftyFifty() {
        double[] bias = domain.expressionBias(Shiny.NON_SHINY, Shiny.NON_SHINY);

        assertEquals(0.5, bias[0], 1e-9);
        assertEquals(0.5, bias[1], 1e-9);
        assertEquals(1.0, bias[0] + bias[1], 1e-9);
    }

    @Test
    void expressionBias_shinyFirst_dominatesNonShiny() {
        double[] bias = domain.expressionBias(Shiny.SHINY, Shiny.NON_SHINY);

        assertEquals(1.0, bias[0], 1e-9);
        assertEquals(0.0, bias[1], 1e-9);
    }

    @Test
    void expressionBias_shinySecond_dominatesNonShiny() {
        double[] bias = domain.expressionBias(Shiny.NON_SHINY, Shiny.SHINY);

        assertEquals(0.0, bias[0], 1e-9);
        assertEquals(1.0, bias[1], 1e-9);
    }

    @Test
    void sampleExpressionOrder_sameAllele_alwaysReturnsSamePair() {
        Random random = new Random(123);

        for (int i = 0; i < 100; i++) {
            AllelePair<Shiny> pair = domain.sampleExpressionOrder(Shiny.SHINY, Shiny.SHINY, random);

            assertEquals(Shiny.SHINY, pair.getFirst());
            assertEquals(Shiny.SHINY, pair.getSecond());
        }
    }

    @Test
    void sampleExpressionOrder_preservesInputAlleles() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Shiny> pair = domain.sampleExpressionOrder(Shiny.SHINY, Shiny.NON_SHINY, random);

            Set<Shiny> alleles = EnumSet.of(pair.getFirst(), pair.getSecond());
            assertEquals(EnumSet.of(Shiny.SHINY, Shiny.NON_SHINY), alleles);
        }
    }

    @Test
    void sampleExpressionOrder_shinyAlwaysExpressed_whenPairedWithNonShiny() {
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            AllelePair<Shiny> pair = domain.sampleExpressionOrder(Shiny.SHINY, Shiny.NON_SHINY, random);

            assertEquals(Shiny.SHINY, pair.getFirst(), "shiny should always be expressed");
            assertEquals(Shiny.NON_SHINY, pair.getSecond());
        }
    }
}
