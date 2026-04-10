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
}
