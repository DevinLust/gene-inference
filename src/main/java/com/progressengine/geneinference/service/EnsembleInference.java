package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ensemble")
public class EnsembleInference extends BaseInferenceEngine {
    private final RelationshipService relationshipService;

    public EnsembleInference(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }


    // Updates the relationship with a new joint distribution using purely multinomial distributions
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
                intermediateScores.put(gradePair, multiScore);
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

        Map<Grade, Double> parent1NewMarginalProbabilities = ensembleMarginalProbability(parent1);
        Map<Grade, Double> parent2NewMarginalProbabilities = ensembleMarginalProbability(parent2);

        parent1.setHiddenDistribution(parent1NewMarginalProbabilities);
        parent2.setHiddenDistribution(parent2NewMarginalProbabilities);
    }

    private Map<Grade, Double> ensembleMarginalProbability(Sheep parent) {
        List<Relationship> allRelationships = relationshipService.findRelationshipsByParent(parent);
        List<Map<Grade, Double>> marginalProbabilities = new ArrayList<>();

        for (Relationship relationship : allRelationships) {
            boolean firstParent = relationship.getParent1().equals(parent);
            Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();
            Map<Grade, Double> newMarginalProbabilities = new EnumMap<>(Grade.class);

            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair gradePair = entry.getKey();
                double newProbability = entry.getValue();
                if (firstParent) {
                    newMarginalProbabilities.merge(gradePair.getFirst(), newProbability, Double::sum);
                } else {
                    newMarginalProbabilities.merge(gradePair.getSecond(), newProbability, Double::sum);
                }
            }

            marginalProbabilities.add(newMarginalProbabilities);
        }

        Map<Grade, Double> ensembleProbabilities = parent.getHiddenDistribution();
        for (Map<Grade, Double> marginalProbability : marginalProbabilities) {
            productOfExperts(ensembleProbabilities, marginalProbability);
        }

        return ensembleProbabilities;
    }

}
