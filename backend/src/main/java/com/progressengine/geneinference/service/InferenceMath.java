package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public final class InferenceMath {

    // combine existing distribution with new distribution
    public static void productOfExperts(Map<Grade, Double> existingDistribution, Map<Grade, Double> newDistribution) {

        for (Map.Entry<Grade, Double> entry : existingDistribution.entrySet()) {
            double newProbability = newDistribution.get(entry.getKey());
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
    }

    // Returns the probability the given allele came from each parent given the assumed hidden alleles
    public static double[] probabilityAlleleFromParents(GradePair hiddenAlleles, Grade phenotype1, Grade phenotype2, Grade allele) {
        double[] probabilities = new double[2];

        double totalProbabilityOfAllele = 0.0;
        double probabilityOfAlleleGivenParent1 = 0.0;
        if (phenotype1.equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (hiddenAlleles.getFirst().equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        double probabilityOfAlleleGivenParent2 = 0.0;
        if (phenotype2.equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (hiddenAlleles.getSecond().equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        if (totalProbabilityOfAllele == 0) {
            return probabilities;
        }

        // multiply each ratio by 0.5 as the probability that each parent is chosen
        probabilities[0] = (0.5 * probabilityOfAlleleGivenParent1) /  totalProbabilityOfAllele;
        probabilities[1] = (0.5 * probabilityOfAlleleGivenParent2) /  totalProbabilityOfAllele;
        return probabilities;
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
}
