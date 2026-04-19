package com.progressengine.geneinference.service.AlleleDomains;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.EnumMap;

import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;

public interface AlleleDomain<A extends Enum<A> & Allele> {
    Set<Category> supportedCategories();
    Class<A> getAlleleType();
    List<A> getAlleles();
    A parse(String code);

    A defaultPhenotype();

    double[] expressionBias(A first, A second);

    AllelePair<A> sampleExpressionOrder(A first, A second, Random random);

    default Map<A, Double> createZeroDistribution() {
        Map<A, Double> result = new EnumMap<>(getAlleleType());
        for (A allele : getAlleles()) {
            result.put(allele, 0.0);
        }
        return result;
    }

    default A evolvePhenotype(A phenotype) {
        throw new UnsupportedOperationException(
                "Phenotype evolution is not supported for domain " + this.getClass().getSimpleName()
        );
    }

    default Map<A, Double> hiddenPriorGivenPhenotype(A phenotype) {
        // default: no extra information → uniform prior
        Map<A, Double> result = new EnumMap<>(getAlleleType());
        double uniform = 1.0 / getAlleles().size();

        for (A allele : getAlleles()) {
            result.put(allele, uniform);
        }

        return result;
    }

    default boolean isHiddenAllelePossible(A phenotype, A hidden) {
        if (phenotype == null) return true; // let other validators handle null phenotype
        if (hidden == null) return true;    // hidden is optional in many flows

        return hiddenPriorGivenPhenotype(phenotype).getOrDefault(hidden, 0.0) > 0.0;
    }
}
