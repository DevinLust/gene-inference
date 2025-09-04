package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service("naive")
public class NaiveInference extends BaseInferenceEngine {

    // Updates the relationship with a new joint distribution using naive bayes and multinomial distributions
    public void findJointDistribution(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        for (Category category : Category.values()) {
            Map<GradePair, Double> intermediateScores = new HashMap<>();
            Map<Grade, Integer> phenotypeFrequency = relationship.getPhenotypeFrequencies(category);
            Grade phenotype1 = parent1.getPhenotype(category);
            Grade phenotype2 = parent2.getPhenotype(category);

            for (Grade grade1 : Grade.values()) {
                for (Grade grade2 : Grade.values()) {
                    GradePair gradePair = new GradePair(grade1, grade2);
                    double multiScore = multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency);
                    intermediateScores.put(gradePair, multiScore * parent1.getDistribution(category, DistributionType.INFERRED).get(grade1) * parent2.getDistribution(category, DistributionType.INFERRED).get(grade2));
                }
            }

            normalizeScores(intermediateScores);
            relationship.setJointDistribution(category, intermediateScores);
        }
    }

    // Returns a new Map of grades to probability given the relationship and observed phenotype of the child
    public void inferChildHiddenDistribution(Relationship relationship, Sheep child) {
        for (Category category : Category.values()) {
            Map<Grade, Double> childHiddenDistribution = new EnumMap<>(Grade.class);
            Grade childPhenotype = child.getPhenotype(category);

            // find the probability distribution of the hidden allele given both genotypes
            Map<GradePair, Map<Grade, Double>> conditionalDistributions = findConditionalDistributions(relationship, childPhenotype, category);

            // sum all conditional distributions from each genotype multiplied by the joint probability of that genotype
            Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);
            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair gradePair = entry.getKey();
                double jointProbability = entry.getValue();
                Map<Grade, Double> genotypeDistribution = conditionalDistributions.get(gradePair);

                // merge each conditional distribution
                for (Map.Entry<Grade, Double> genotypeDistributionEntry : genotypeDistribution.entrySet()) {
                    Grade grade = genotypeDistributionEntry.getKey();
                    double conditionalProbability = genotypeDistributionEntry.getValue();
                    childHiddenDistribution.merge(grade, conditionalProbability * jointProbability, Double::sum);
                }
            }

            // fill any missing probabilities with 0.0
            fillMissingValuesWithZero(childHiddenDistribution);

            child.setDistribution(category, DistributionType.PRIOR, childHiddenDistribution);
        }
    }

    // updates the hidden distributions of each parent in the relationship
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);

            Map<Grade, Double> parent1NewMarginalProbabilities = new EnumMap<>(Grade.class);
            Map<Grade, Double> parent2NewMarginalProbabilities = new EnumMap<>(Grade.class);

            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair gradePair = entry.getKey();
                double jointProbability = entry.getValue();

                parent1NewMarginalProbabilities.merge(gradePair.getFirst(), jointProbability, Double::sum);
                parent2NewMarginalProbabilities.merge(gradePair.getSecond(), jointProbability, Double::sum);
            }

            parent1.setDistribution(category, DistributionType.INFERRED, parent1NewMarginalProbabilities);
            parent2.setDistribution(category, DistributionType.INFERRED, parent2NewMarginalProbabilities);
        }
    }

}
