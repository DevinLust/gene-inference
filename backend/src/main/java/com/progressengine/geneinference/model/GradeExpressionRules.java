package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Grade;

import java.util.Random;

public final class GradeExpressionRules {

    private static final double DOMINANT_EXPRESSION_PROBABILITY = 0.7;

    private GradeExpressionRules() {}

    public static double[] probabilityExpressed(Grade first, Grade second) {
        if (first == second) {
            return new double[]{0.5, 0.5};
        }

        return first.isBetterThan(second) ? new double[]{DOMINANT_EXPRESSION_PROBABILITY, 1 - DOMINANT_EXPRESSION_PROBABILITY} : new double[]{1 - DOMINANT_EXPRESSION_PROBABILITY, DOMINANT_EXPRESSION_PROBABILITY};
    }

    public static GradePair sampleExpressionOrder(Grade first, Grade second, Random random) {
        if (first == second) {
            return new GradePair(first, second);
        }

        Grade better = first.isBetterThan(second) ? first : second;
        Grade worse = first.isBetterThan(second) ? second : first;

        return random.nextDouble() < DOMINANT_EXPRESSION_PROBABILITY
                ? new GradePair(better, worse)
                : new GradePair(worse, better);
    }
}