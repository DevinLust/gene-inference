package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradeExpressionRules;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.*;

public final class InferenceMath {

    public static <T> void validateDistribution(Map<T, Double> distribution) {
        // Validate sum ≈ 1.0
        double total = distribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(total - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Distribution probabilities must sum to 1.0 (±1e-6). Actual sum: " + total);
        }
    }


    // combine existing distribution with new distribution
    public static <T> void productOfExperts(Map<T, Double> existingDistribution, Map<T, Double> newDistribution) {
        for (Map.Entry<T, Double> entry : existingDistribution.entrySet()) {
            T key = entry.getKey();
            double newProbability = newDistribution.getOrDefault(key, 0.0);
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
        validateDistribution(existingDistribution);
    }


    // Returns the probability the given allele came from each parent given the assumed hidden alleles
    public static double[] probabilityAlleleFromParents(
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

    public static Map<Grade, Double> childHiddenDistributionGivenParents(
            GradePair hiddenPair,
            Grade parent1Phenotype,
            Grade parent2Phenotype,
            Grade childPhenotype
    ) {
        Map<Grade, Double> result = new EnumMap<>(Grade.class);
        fillMissingValuesWithZero(result);

        Grade[] parent1Alleles = {parent1Phenotype, hiddenPair.getFirst()};
        Grade[] parent2Alleles = {parent2Phenotype, hiddenPair.getSecond()};

        for (Grade allele1 : parent1Alleles) {
            for (Grade allele2 : parent2Alleles) {
                double inheritanceProb = 0.25;
                double[] expressionProbabilities = GradeExpressionRules.probabilityExpressed(allele1, allele2);

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


    public static Map<GradePair, Double> multinomialJointScores(Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        Map<GradePair, Double> multinomialDistribution = new HashMap<>();
        for (Grade grade1 : Grade.values()) {
            for (Grade grade2 : Grade.values()) {
                GradePair gradePair = new GradePair(grade1, grade2);
                double multiScore = multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency);
                multinomialDistribution.put(gradePair, multiScore);
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


    public static void fillMissingValuesWithZero(Map<Grade, Double> scores) {
        for (Grade grade : Grade.values()) {
            scores.putIfAbsent(grade, 0.0);
        }
    }


    // Returns a relative multinomial score based on the given hidden alleles, phenotypes, and phenotype frequency seen in the relationship
    public static double multinomialScore(
            GradePair hiddenPair,
            Grade phenotype1,
            Grade phenotype2,
            Map<Grade, Integer> phenotypeFrequency
    ) {
        double score = 1_000_000.0;

        Map<Grade, Double> phenotypeProbabilities =
                childPhenotypeDistribution(hiddenPair, phenotype1, phenotype2);

        for (Grade grade : Grade.values()) {
            double probability = phenotypeProbabilities.getOrDefault(grade, 0.0);
            int frequency = phenotypeFrequency.getOrDefault(grade, 0);

            if (probability == 0.0 && frequency > 0) {
                return 0.0;
            }

            if (frequency > 0) {
                score *= Math.exp(frequency * Math.log(probability));
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

    private static Map<Grade, Double> childPhenotypeDistribution(
            GradePair hiddenPair,
            Grade phenotype1,
            Grade phenotype2
    ) {
        Map<Grade, Double> distribution = new EnumMap<>(Grade.class);

        Grade[] parent1Alleles = {phenotype1, hiddenPair.getFirst()};
        Grade[] parent2Alleles = {phenotype2, hiddenPair.getSecond()};

        for (Grade allele1 : parent1Alleles) {
            for (Grade allele2 : parent2Alleles) {
                double inheritanceProb = 0.25;
                double[] expressionProbabilities = GradeExpressionRules.probabilityExpressed(allele1, allele2);

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
