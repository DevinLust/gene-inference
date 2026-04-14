package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Shiny;

import java.util.*;

public class ShinyAlleleDomain implements AlleleDomain<Shiny> {

    private static final double DOMINANT_EXPRESSION_PROBABILITY = 1.0;

    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(
                Category.SHINY
        );
    }

    @Override
    public Class<Shiny> getAlleleType() {
        return Shiny.class;
    }

    @Override
    public List<Shiny> getAlleles() {
        return List.of(Shiny.values());
    }

    @Override
    public Shiny parse(String code) {
        return Shiny.fromCode(code);
    }

    @Override
    public double[] expressionBias(Shiny first, Shiny second) {
        if (first == second) {
            return new double[]{0.5, 0.5};
        }

        return second.isRecessive() ? new double[]{DOMINANT_EXPRESSION_PROBABILITY, 1 - DOMINANT_EXPRESSION_PROBABILITY} : new double[]{1 - DOMINANT_EXPRESSION_PROBABILITY, DOMINANT_EXPRESSION_PROBABILITY};
    }

    @Override
    public AllelePair<Shiny> sampleExpressionOrder(Shiny first, Shiny second, Random random) {
        if (first == second) {
            return new AllelePair<>(first, second);
        }

        return second.isRecessive()
                ? new AllelePair<>(first, second)
                : new AllelePair<>(second, first);
    }

    @Override
    public Map<Shiny, Double> hiddenPriorGivenPhenotype(Shiny phenotype) {
        Map<Shiny, Double> result = createZeroDistribution();

        if (phenotype == Shiny.NON_SHINY) {
            result.put(Shiny.NON_SHINY, 1.0);
            return result;
        }

        // SHINY → could be SHINY or NON_SHINY
        result.put(Shiny.SHINY, 0.5);
        result.put(Shiny.NON_SHINY, 0.5);

        return result;
    }

    @Override
    public boolean isHiddenAllelePossible(Shiny phenotype, Shiny hidden) {
        if (hidden == null) {
            return true;
        }
        if (phenotype == Shiny.NON_SHINY) {
            return hidden == Shiny.NON_SHINY;
        }
        return true;
    }
}
