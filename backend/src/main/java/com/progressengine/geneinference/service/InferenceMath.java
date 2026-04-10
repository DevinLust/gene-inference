package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.GradeExpressionRules;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import java.util.*;
import java.util.stream.Collectors;

public final class InferenceMath {

    /**
     * Predicts the probabilities of the phenotype for each category of the two parents' children.
     * The two parents don't need to be in a Relationship. The two parents can be the same
     * Sheep.
     *
     * @param parent1 - the first parent Sheep to base the prediction on
     * @param parent2 - the second parent Sheep to base the prediction on
     * @return a Map of distributions that predict the probability of phenotypes for the children
     * of these two parents by category
     */
    public static Map<Category, Map<String, Double>> predictChildrenDistributions(Sheep parent1, Sheep parent2) {
        Map<Category, Map<String, Double>> predictedDistributions = new EnumMap<>(Category.class);

        for (Category category : Category.values()) {
            Map<String, Double> childPhenotypeDistribution = predictedChildDistributionByCategory(parent1, parent2, category);

            predictedDistributions.put(category, childPhenotypeDistribution);
        }

        return predictedDistributions;
    }

    private static <A extends Enum<A> & Allele> Map<String, Double> predictedChildDistributionByCategory(Sheep parent1, Sheep parent2, Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> parent1AlleleDistribution = inheritedAlleleDistribution(parent1, category);
        Map<A, Double> parent2AlleleDistribution = inheritedAlleleDistribution(parent2, category);

        Map<A, Double> childPhenotypeDistribution = new EnumMap<>(domain.getAlleleType());
        for (A expressed : domain.getAlleles()) {
            childPhenotypeDistribution.put(expressed, 0.0);
        }

        for (Map.Entry<A, Double> p1Entry : parent1AlleleDistribution.entrySet()) {
            A allele1 = p1Entry.getKey();
            double pAllele1 = p1Entry.getValue();

            for (Map.Entry<A, Double> p2Entry : parent2AlleleDistribution.entrySet()) {
                A allele2 = p2Entry.getKey();
                double pAllele2 = p2Entry.getValue();

                double genotypeProbability = pAllele1 * pAllele2;
                double[] expressionProbability = domain.expressionBias(allele1, allele2);

                childPhenotypeDistribution.merge(
                        allele1,
                        genotypeProbability * expressionProbability[0],
                        Double::sum
                );
                childPhenotypeDistribution.merge(
                        allele2,
                        genotypeProbability * expressionProbability[1],
                        Double::sum
                );
            }
        }

        return childPhenotypeDistribution.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().code(), 
                Map.Entry::getValue
            ));
    }

    private static <A extends Enum<A> & Allele> Map<A, Double> inheritedAlleleDistribution(Sheep parent, Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> result = new EnumMap<>(domain.getAlleleType());

        // 50% chance to pass the visible allele
        A phenotype = parent.getPhenotype(category);
        result.merge(phenotype, 0.5, Double::sum);

        // 50% chance to pass the hidden allele, distributed by the inferred hidden distribution
        Map<A, Double> hiddenDistribution = parent.getDistribution(category, DistributionType.INFERRED);
        for (Map.Entry<A, Double> entry : hiddenDistribution.entrySet()) {
            result.merge(entry.getKey(), 0.5 * entry.getValue(), Double::sum);
        }

        return result;
    }

    public static <A> void validateDistribution(Map<A, Double> distribution) {
        // Validate sum ≈ 1.0
        double total = distribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(total - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Distribution probabilities must sum to 1.0 (±1e-6). Actual sum: " + total);
        }
    }


    // combine existing distribution with new distribution
    public static <A> void productOfExperts(Map<A, Double> existingDistribution, Map<A, Double> newDistribution) {
        for (Map.Entry<A, Double> entry : existingDistribution.entrySet()) {
            A key = entry.getKey();
            double newProbability = newDistribution.getOrDefault(key, 0.0);
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
        validateDistribution(existingDistribution);
    }


    // Returns the probability the given allele came from each parent given the assumed hidden alleles
    @Deprecated
    public static double[] probabilityAlleleFromParents( // TODO - update for generic alleles
            GradePair hiddenAlleles,
            Grade phenotype1,
            Grade phenotype2,
            Grade observedPhenotype
    ) {
        double[] probabilities = new double[2];

        Grade hidden1 = hiddenAlleles.getFirst();
        Grade hidden2 = hiddenAlleles.getSecond();

        Grade[] parent1Alleles = {phenotype1, hidden1};
        Grade[] parent2Alleles = {phenotype2, hidden2};

        for (Grade allele1 : parent1Alleles) {
            for (Grade allele2 : parent2Alleles) {
                double inheritanceProb = 0.25;
                double[] expressionProbabilities = GradeExpressionRules.probabilityExpressed(allele1, allele2);

                if (allele1 == observedPhenotype) {
                    double expressedFromParent1 = inheritanceProb * expressionProbabilities[0];
                    probabilities[0] += expressedFromParent1;
                }

                if (allele2 == observedPhenotype) {
                    double expressedFromParent2 = inheritanceProb * expressionProbabilities[1];
                    probabilities[1] += expressedFromParent2;
                }
            }
        }
        double totalProbability = probabilities[0] + probabilities[1];
        if (totalProbability == 0.0) {
            return probabilities;
        }

        probabilities[0] /= totalProbability;
        probabilities[1] /= totalProbability;
        return probabilities;
    }

    public static <A extends Enum<A> & Allele> Map<A, Double> childHiddenDistributionGivenParents( // TODO - update for generic alleles
            AllelePair<A> hiddenPair,
            A parent1Phenotype,
            A parent2Phenotype,
            A childPhenotype,
            AlleleDomain<A> domain
    ) {
        Map<A, Double> result = new EnumMap<>(domain.getAlleleType());
        fillMissingValuesWithZero(result, domain);

        List<A> parent1Alleles = List.of(parent1Phenotype, hiddenPair.getFirst());
        List<A> parent2Alleles = List.of(parent2Phenotype, hiddenPair.getSecond());

        for (A allele1 : parent1Alleles) {
            for (A allele2 : parent2Alleles) {
                double inheritanceProb = 0.25;
                double[] expressionProbabilities = domain.expressionBias(allele1, allele2);

                // If allele1 is expressed as the observed phenotype,
                // then the hidden allele is allele2.
                if (allele1 == childPhenotype) {
                    result.merge(
                            allele2,
                            inheritanceProb * expressionProbabilities[0],
                            Double::sum
                    );
                }

                // If allele2 is expressed as the observed phenotype,
                // then the hidden allele is allele1.
                if (allele2 == childPhenotype) {
                    result.merge(
                            allele1,
                            inheritanceProb * expressionProbabilities[1],
                            Double::sum
                    );
                }
            }
        }

        normalizeScores(result);
        return result;
    }


    public static <A extends Enum<A> & Allele> Map<AllelePair<A>, Double> multinomialJointScores(
        A phenotype1, 
        A phenotype2, 
        Map<A, Integer> phenotypeFrequency, 
        AlleleDomain<A> domain) 
    {
        Map<AllelePair<A>, Double> multinomialDistribution = new HashMap<>();
        for (A allele1 : domain.getAlleles()) {
            for (A allele2 : domain.getAlleles()) {
                AllelePair<A> allelePair = new AllelePair<>(allele1, allele2);
                double multiScore = multinomialScore(allelePair, phenotype1, phenotype2, phenotypeFrequency, domain);
                multinomialDistribution.put(allelePair, multiScore);
            }
        }
        normalizeScores(multinomialDistribution);
        validateDistribution(multinomialDistribution);
        return multinomialDistribution;
    }


    // normalize the given Map of scores regardless of the key type
    public static <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }


    public static <A extends Enum<A> & Allele> void fillMissingValuesWithZero(Map<A, Double> scores, AlleleDomain<A> domain) {
        for (A allele : domain.getAlleles()) {
            scores.putIfAbsent(allele, 0.0);
        }
    }


    // Returns a relative multinomial score based on the given hidden alleles, phenotypes, and phenotype frequency seen in the relationship
    public static <A extends Enum<A> & Allele> double multinomialScore( // TODO - update for generic alleles
            AllelePair<A> hiddenPair,
            A phenotype1,
            A phenotype2,
            Map<A, Integer> phenotypeFrequency,
            AlleleDomain<A> domain
    ) {
        double score = 1_000_000.0;

        Map<A, Double> phenotypeProbabilities =
                childPhenotypeDistribution(hiddenPair, phenotype1, phenotype2, domain);

        for (A allele : domain.getAlleles()) {
            double probability = phenotypeProbabilities.getOrDefault(allele, 0.0);
            int frequency = phenotypeFrequency.getOrDefault(allele, 0);

            if (probability == 0.0 && frequency > 0) {
                return 0.0;
            }

            if (frequency > 0) {
                score *= Math.exp(frequency * Math.log(probability)); // = probability ^ frequency
            }
        }

        return score;
    }


    public static <T> double entropy(Map<T, Double> distribution) {
        double entropy = 0.0;

        for (double p : distribution.values()) {
            if (p > 0.0) {  // avoid log(0)
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        return entropy;
    }

    public static <A extends Enum<A> & Allele> Map<A, Double> childPhenotypeDistribution(
            AllelePair<A> hiddenPair,
            A phenotype1,
            A phenotype2,
            AlleleDomain<A> domain
    ) {
        Map<A, Double> distribution = new EnumMap<>(domain.getAlleleType());

        List<A> parent1Alleles = List.of(phenotype1, hiddenPair.getFirst());
        List<A> parent2Alleles = List.of(phenotype2, hiddenPair.getSecond());

        for (A allele1 : parent1Alleles) {
            for (A allele2 : parent2Alleles) {
                double inheritanceProb = 0.25;
                double[] expressionProbabilities = domain.expressionBias(allele1, allele2);

                distribution.merge(
                        allele1,
                        inheritanceProb * expressionProbabilities[0],
                        Double::sum
                );

                distribution.merge(
                        allele2,
                        inheritanceProb * expressionProbabilities[1],
                        Double::sum
                );
            }
        }

        return distribution;
    }
}
