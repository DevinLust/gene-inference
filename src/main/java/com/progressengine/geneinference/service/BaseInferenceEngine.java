package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
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
    protected Map<GradePair, Map<Grade, Double>> findConditionalDistributions(Relationship relationship, Grade childPhenotype, Category category) {
        Grade phenotype1 = relationship.getParent1().getPhenotype(category);
        Grade phenotype2 = relationship.getParent2().getPhenotype(category);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);

        // find the probability distribution of the hidden allele given both genotypes
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = new HashMap<>();
        for (GradePair gradePair : jointDistribution.keySet()) {
            // find the probability the phenotype came from each parent
            double[] parentProbabilities = probabilityAlleleFromParents(gradePair, phenotype1, phenotype2, childPhenotype);

            // get the distribution of the hidden child allele from the current genotypes
            Map<Grade, Double> genotypeDistribution = new EnumMap<>(Grade.class);
            genotypeDistribution.merge(phenotype1, 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(gradePair.getFirst(), 0.5 * parentProbabilities[1], Double::sum);
            genotypeDistribution.merge(phenotype2, 0.5 * parentProbabilities[0], Double::sum);
            genotypeDistribution.merge(gradePair.getSecond(), 0.5 * parentProbabilities[0], Double::sum);

            conditionalDistributions.put(gradePair, genotypeDistribution);
        }

        return conditionalDistributions;
    }

    // Returns the probability the given allele came from each parent given the assumed hidden alleles
    protected double[] probabilityAlleleFromParents(GradePair hiddenAlleles, Grade phenotype1, Grade phenotype2, Grade allele) {
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

    // Returns a relative multinomial score based on the given hidden alleles, phenotypes, and phenotype frequency seen in the relationship
    protected double multinomialScore(GradePair hiddenPair, Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        double score = 1000000.0; // A multiplicative constant to help keep scores from getting too small quickly

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

    protected void fillMissingValuesWithZero(Map<Grade, Double> scores) {
        for (Grade grade : Grade.values()) {
            scores.putIfAbsent(grade, 0.0);
        }
    }
}
