package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseInferenceEngine implements InferenceEngine {
    // combine existing distribution with new distribution
    protected void productOfExperts(Map<Grade, Double> existingDistribution, Map<Grade, Double> newDistribution) {

        for (Map.Entry<Grade, Double> entry : existingDistribution.entrySet()) {
            double newProbability = newDistribution.get(entry.getKey());
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
    }

    // Maps a pair of hidden alleles to a conditional distribution based on the observed phenotype
    protected Map<GradePair, Map<Grade, Double>> findConditionalDistributions(Relationship relationship, Grade childPhenotype) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();

        // find the probability distribution of the hidden allele given both genotypes
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = new HashMap<>();
        for (GradePair gradePair : jointDistribution.keySet()) {
            // find the probability the phenotype came from each parent
            double[] parentProbabilities = probabilityAlleleFromParents(gradePair, parent1, parent2, childPhenotype);

            // get the distribution of the hidden child allele from the current genotypes
            Map<Grade, Double> genotypeDistribution = new EnumMap<>(Grade.class);
            genotypeDistribution.merge(parent1.getPhenotype(), 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(gradePair.getFirst(), 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(parent2.getPhenotype(), 0.5 * parentProbabilities[0], Double::sum);
            genotypeDistribution.merge(gradePair.getSecond(), 0.5 * parentProbabilities[0], Double::sum);

            conditionalDistributions.put(gradePair, genotypeDistribution);
        }

        return conditionalDistributions;
    }

    protected double[] probabilityAlleleFromParents(GradePair gradePair, Sheep parent1, Sheep parent2, Grade allele) {
        double[] probabilities = new double[2];

        double totalProbabilityOfAllele = 0.0;
        double probabilityOfAlleleGivenParent1 = 0.0;
        if (parent1.getPhenotype().equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (gradePair.getFirst().equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        double probabilityOfAlleleGivenParent2 = 0.0;
        if (parent2.getPhenotype().equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (gradePair.getSecond().equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        if (totalProbabilityOfAllele == 0) {
            return probabilities;
        }

        probabilities[0] = (0.5 * probabilityOfAlleleGivenParent1) /  totalProbabilityOfAllele;
        probabilities[1] = (0.5 * probabilityOfAlleleGivenParent2) /  totalProbabilityOfAllele;
        return probabilities;
    }

    protected double multinomialScore(GradePair hiddenPair, Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        double score = 1000000.0;

        // each occurrence of a grade adds 1/4 to the probability of that grade
        Map<Grade, Double> probabilityToDraw = new EnumMap<>(Grade.class);
        probabilityToDraw.merge(phenotype1, 0.25, Double::sum);
        probabilityToDraw.merge(phenotype2, 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getFirst(), 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getSecond(), 0.25, Double::sum);

        for (Grade grade : Grade.values()) {
            Double probability = probabilityToDraw.getOrDefault(grade, 0.0);
            Integer frequency = phenotypeFrequency.getOrDefault(grade, 0);
            if (probability == 0.0 && frequency > 0) {
                score = 0.0;
            } else if (frequency > 0) {
                score *= Math.exp(frequency * Math.log(probability));
            }
        }

        return score;
    }

    // normalize the given Map of scores regardless of the key type
    protected <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }
}
