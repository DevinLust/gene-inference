package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Color;

import java.util.*;

public class ColorAlleleDomain implements AlleleDomain<Color> {

    private static final double DOMINANT_EXPRESSION_PROBABILITY = 1.0;

    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(
                Category.COLOR
        );
    }

    @Override
    public Class<Color> getAlleleType() {
        return Color.class;
    }

    @Override
    public List<Color> getAlleles() {
        return List.of(Color.values());
    }

    @Override
    public Color parse(String code) {
        return Color.fromCode(code);
    }

    @Override
    public Color defaultPhenotype() {
        return Color.NORMAL;
    }

    @Override
    public double[] expressionBias(Color first, Color second) {
        if (first == second) {
            return new double[]{0.5, 0.5};
        }
        if ((first.isRecessive() && second.isRecessive()) || (!first.isRecessive() && !second.isRecessive())) {
            return new double[]{0.5, 0.5};
        }

        return second.isRecessive() ? new double[]{DOMINANT_EXPRESSION_PROBABILITY, 1 - DOMINANT_EXPRESSION_PROBABILITY} : new double[]{1 - DOMINANT_EXPRESSION_PROBABILITY, DOMINANT_EXPRESSION_PROBABILITY};
    }

    @Override
    public AllelePair<Color> sampleExpressionOrder(Color first, Color second, Random random) {
        if (first == second) {
            return new AllelePair<>(first, second);
        }
        if ((first.isRecessive() && second.isRecessive()) || (!first.isRecessive() && !second.isRecessive())) {
            return random.nextBoolean()
                    ? new AllelePair<>(first, second)
                    : new AllelePair<>(second, first);
        }

        return second.isRecessive()
                ? new AllelePair<>(first, second)
                : new AllelePair<>(second, first);
    }

    @Override
    public Map<Color, Double> hiddenPriorGivenPhenotype(Color phenotype) {
        Map<Color, Double> result = createZeroDistribution();

        if (phenotype == Color.NORMAL) {
            result.put(Color.NORMAL, 1.0);
            return result;
        }

        double uniform = 1.0 / getAlleles().size();
        for (Color color : getAlleles()) {
            result.put(color, uniform);
        }

        return result;
    }

    @Override
    public boolean isHiddenAllelePossible(Color phenotype, Color hidden) {
        if (hidden == null) return true;
        if (phenotype == Color.NORMAL) {
            return hidden == Color.NORMAL;
        }
        return true;
    }
}
