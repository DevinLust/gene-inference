package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class NaiveInference implements InferenceEngine {

    // Updates the relationship with a new joint distribution using naive bayes and multinomial distributions
    public void findJointDistribution(Relationship relationship) {
        Map<GradePair, Double> intermediateScores = new HashMap<>();
        Map<Grade, Integer> phenotypeFrequency = relationship.getOffspringPhenotypeFrequency();
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        Grade phenotype1 = parent1.getPhenotype();
        Grade phenotype2 = parent2.getPhenotype();

        for (Grade grade1 : Grade.values()) {
            for (Grade grade2 : Grade.values()) {
                GradePair gradePair = new GradePair(grade1, grade2);
                double multiScore = multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency);
                intermediateScores.put(gradePair, multiScore * parent1.getHiddenDistribution().get(grade1) * parent2.getHiddenDistribution().get(grade2));
            }
        }

        normalizeScores(intermediateScores);
        relationship.setHiddenPairsDistribution(intermediateScores);
    }

    // Returns a new Map of grades to probability given the relationship and observed phenotype of the child
    public Map<Grade, Double> inferChildHiddenDistribution(Relationship relationship, Grade childPhenotype) {
        Map<Grade, Double> childHiddenDistribution = new EnumMap<>(Grade.class);

        // find the probability distribution of the hidden allele given both genotypes
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = findConditionalDistributions(relationship, childPhenotype);

        // sum all conditional distributions from each genotype multiplied by the joint probability of that genotype
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();
        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double jointProbability =  entry.getValue();
            Map<Grade, Double> genotypeDistribution = conditionalDistributions.get(gradePair);

            // merge each conditional distribution
            for (Map.Entry<Grade, Double> genotypeDistributionEntry : genotypeDistribution.entrySet()) {
                Grade grade = genotypeDistributionEntry.getKey();
                double conditionalProbability = genotypeDistributionEntry.getValue();
                childHiddenDistribution.merge(grade, conditionalProbability * jointProbability, Double::sum);
            }
        }

        // fill any missing probabilities with 0.0
        for (Grade grade : Grade.values()) {
            childHiddenDistribution.putIfAbsent(grade, 0.0);
        }

        return childHiddenDistribution;
    }

    // updates the hidden distributions of each parent in the relationship
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();

        Map<Grade, Double> parent1NewMarginalProbabilities = new EnumMap<>(Grade.class);
        Map<Grade, Double> parent2NewMarginalProbabilities = new EnumMap<>(Grade.class);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double jointProbability = entry.getValue();

            parent1NewMarginalProbabilities.merge(gradePair.getFirst(), jointProbability, Double::sum);
            parent2NewMarginalProbabilities.merge(gradePair.getSecond(), jointProbability, Double::sum);
        }

        // productOfExperts(parent1, parent1NewMarginalProbabilities);
        // productOfExperts(parent2, parent2NewMarginalProbabilities);
        parent1.setHiddenDistribution(parent1NewMarginalProbabilities);
        parent2.setHiddenDistribution(parent2NewMarginalProbabilities);
    }

    // combine existing distribution with new distribution
    private void productOfExperts(Sheep sheep, Map<Grade, Double> newDistribution) {
        Map<Grade, Double> existingDistribution = sheep.getHiddenDistribution();

        for (Map.Entry<Grade, Double> entry : newDistribution.entrySet()) {
            double existingProbability = existingDistribution.get(entry.getKey());
            entry.setValue(entry.getValue() * existingProbability);
        }

        normalizeScores(newDistribution);
        sheep.setHiddenDistribution(newDistribution);
    }

    // Maps a pair of hidden alleles to a conditional distribution based on the observed phenotype
    private Map<GradePair, Map<Grade, Double>> findConditionalDistributions(Relationship relationship, Grade childPhenotype) {
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

    private double[] probabilityAlleleFromParents(GradePair gradePair, Sheep parent1, Sheep parent2, Grade allele) {
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

    private double multinomialScore(GradePair hiddenPair, Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
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
    private <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }

}
