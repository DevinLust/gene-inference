package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import com.progressengine.geneinference.service.AlleleDomains.GradeAlleleDomain;

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
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)) {
                continue;
            }
            Map<AllelePair<Grade>, Double> intermediateScores = new HashMap<>();
            Map<Grade, Integer> phenotypeFrequency = relationship.getCurrentPhenotypeFrequencies(category);
            Grade phenotype1 = parent1.getPhenotype(category);
            Grade phenotype2 = parent2.getPhenotype(category);

            for (Grade grade1 : Grade.values()) {
                for (Grade grade2 : Grade.values()) {
                    AllelePair<Grade> gradePair = new AllelePair<>(grade1, grade2);
                    double multiScore = InferenceMath.multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency, gradeDomain());
                    intermediateScores.put(gradePair, multiScore * parent1.getDistribution(category, DistributionType.INFERRED).get(grade1) * parent2.getDistribution(category, DistributionType.INFERRED).get(grade2));
                }
            }

            InferenceMath.normalizeScores(intermediateScores);
            relationship.setJointDistribution(category, intermediateScores);
        }
    }

    // Returns a new Map of grades to probability given the relationship and observed phenotype of the child
    public void inferChildHiddenDistribution(Relationship relationship, Sheep child) {
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)) {
                continue;
            }
            Map<Grade, Double> childHiddenDistribution = new EnumMap<>(Grade.class);
            Grade childPhenotype = child.getPhenotype(category);

            // find the probability distribution of the hidden allele given both genotypes
            Map<AllelePair<Grade>, Map<Grade, Double>> conditionalDistributions = findConditionalDistributions(relationship, childPhenotype, category);

            // sum all conditional distributions from each genotype multiplied by the joint probability of that genotype
            Map<AllelePair<Grade>, Double> jointDistribution = relationship.getJointDistribution(category);
            for (Map.Entry<AllelePair<Grade>, Double> entry : jointDistribution.entrySet()) {
                AllelePair<Grade> gradePair = entry.getKey();
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
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)) {
                continue;
            }
            Map<AllelePair<Grade>, Double> jointDistribution = relationship.getJointDistribution(category);

            Map<Grade, Double> parent1NewMarginalProbabilities = new EnumMap<>(Grade.class);
            Map<Grade, Double> parent2NewMarginalProbabilities = new EnumMap<>(Grade.class);

            for (Map.Entry<AllelePair<Grade>, Double> entry : jointDistribution.entrySet()) {
                AllelePair<Grade> gradePair = entry.getKey();
                double jointProbability = entry.getValue();

                parent1NewMarginalProbabilities.merge(gradePair.getFirst(), jointProbability, Double::sum);
                parent2NewMarginalProbabilities.merge(gradePair.getSecond(), jointProbability, Double::sum);
            }

            parent1.setDistribution(category, DistributionType.INFERRED, parent1NewMarginalProbabilities);
            parent2.setDistribution(category, DistributionType.INFERRED, parent2NewMarginalProbabilities);
        }
    }

}
