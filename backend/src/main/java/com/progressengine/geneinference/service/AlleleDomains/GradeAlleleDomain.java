package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.Random;

@Component
public class GradeAlleleDomain implements AlleleDomain<Grade> {

    private static final double DOMINANT_EXPRESSION_PROBABILITY = 0.7;
    
    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(
                Category.SWIM,
                Category.FLY,
                Category.RUN,
                Category.POWER,
                Category.STAMINA
        );
    }

    @Override
    public Class<Grade> getAlleleType() {
        return Grade.class;
    }

    @Override
    public List<Grade> getAlleles() {
        return List.of(Grade.values());
    }

    @Override
    public Grade parse(String code) {
        return Grade.fromCode(code);
    }

    @Override
    public double[] expressionBias(Grade first, Grade second) {
        if (first == second) {
            return new double[]{0.5, 0.5};
        }

        return first.isBetterThan(second) ? new double[]{DOMINANT_EXPRESSION_PROBABILITY, 1 - DOMINANT_EXPRESSION_PROBABILITY} : new double[]{1 - DOMINANT_EXPRESSION_PROBABILITY, DOMINANT_EXPRESSION_PROBABILITY};
    }

    @Override
    public AllelePair<Grade> sampleExpressionOrder(Grade first, Grade second, Random random) {
        if (first == second) {
            return new AllelePair<>(first, second);
        }

        Grade better = first.isBetterThan(second) ? first : second;
        Grade worse = first.isBetterThan(second) ? second : first;

        return random.nextDouble() < DOMINANT_EXPRESSION_PROBABILITY
                ? new AllelePair<>(better, worse)
                : new AllelePair<>(worse, better);
    }
}
