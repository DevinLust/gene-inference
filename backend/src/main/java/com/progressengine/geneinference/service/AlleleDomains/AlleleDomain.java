package com.progressengine.geneinference.service.AlleleDomains;

import java.util.*;

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

    default Set<A> possibleHiddenAllelesForPhenotype(A phenotype) {
        Set<A> result = EnumSet.noneOf(getAlleleType());
        for (A allele : getAlleles()) {
            if (isHiddenAllelePossible(phenotype, allele)) {
                result.add(allele);
            }
        }
        return result;
    }

    default Set<AllelePair<A>> possibleParentHiddenAssignments(A parent1Phenotype, A parent2Phenotype) {
        Set<A> parent1Possible = possibleHiddenAllelesForPhenotype(parent1Phenotype);
        Set<A> parent2Possible = possibleHiddenAllelesForPhenotype(parent2Phenotype);
        Set<AllelePair<A>> result = new HashSet<>();

        for (A a1 : parent1Possible) {
            for (A a2 : parent2Possible) {
                result.add(new AllelePair<>(a1, a2));
            }
        }

        return result;
    }

    default Set<A> possibleChildPhenotypesFromGenotypes(
            A p1Phenotype, A p1Hidden,
            A p2Phenotype, A p2Hidden
    ) {
        Set<A> result = EnumSet.noneOf(getAlleleType());

        Set<A> p1Set = EnumSet.of(p1Phenotype, p1Hidden);
        Set<A> p2Set = EnumSet.of(p2Phenotype, p2Hidden);

        for (A a1 : p1Set) {
            for (A a2 : p2Set) {
                double[] bias = expressionBias(a1, a2);

                if (bias[0] > 0.0) {
                    result.add(a1);
                }
                if (bias[1] > 0.0) {
                    result.add(a2);
                }
            }
        }

        return result;
    }

    default boolean canProduceChildPhenotype(A p1Phenotype, A p1Hidden, A p2Phenotype, A p2Hidden, A childPhenotype) {
        return possibleChildPhenotypesFromGenotypes(p1Phenotype, p1Hidden, p2Phenotype, p2Hidden).contains(childPhenotype);
    }

    default boolean canProduceChildGenotype(
            A p1Phenotype,
            A p1Hidden,
            A p2Phenotype,
            A p2Hidden,
            A childPhenotype,
            A childHidden
    ) {
        Set<A> p1Alleles = EnumSet.of(p1Phenotype, p1Hidden);
        Set<A> p2Alleles = EnumSet.of(p2Phenotype, p2Hidden);

        for (A fromP1 : p1Alleles) {
            for (A fromP2 : p2Alleles) {
                double[] bias = expressionBias(fromP1, fromP2);

                boolean sameOrder = fromP1 == childPhenotype && fromP2 == childHidden && bias[0] > 0.0;
                boolean reverseOrder = fromP2 == childPhenotype && fromP1 == childHidden && bias[1] > 0.0;

                if (sameOrder || reverseOrder) {
                    return true;
                }
            }
        }

        return false;
    }
}
