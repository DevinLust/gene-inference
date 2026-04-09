package com.progressengine.geneinference.service.AlleleDomains;

import com.progressengine.geneinference.model.enums.Tone;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.Random;

@Component
public class ToneAlleleDomain implements AlleleDomain<Tone> {

    private static final double DOMINANT_EXPRESSION_PROBABILITY = 0.5;

    @Override
    public Set<Category> supportedCategories() {
        return EnumSet.of(Category.TONE);
    }

    @Override
    public Class<Tone> getAlleleType() {
        return Tone.class;
    }

    @Override
    public List<Tone> getAlleles() {
        return List.of(Tone.values());
    }

    @Override
    public Tone parse(String code) {
        return Tone.fromCode(code);
    }

    @Override
    public double[] expressionBias(Tone first, Tone second) {
        return new double[]{0.5, 0.5};
    }

    @Override 
    public AllelePair<Tone> sampleExpressionOrder(Tone first, Tone second, Random random) {
        return random.nextDouble() < DOMINANT_EXPRESSION_PROBABILITY
                ? new AllelePair<>(first, second)
                : new AllelePair<>(second, first);
    }
}
