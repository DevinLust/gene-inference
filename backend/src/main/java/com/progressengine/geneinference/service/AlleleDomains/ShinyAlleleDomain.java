package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Shiny;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
}
