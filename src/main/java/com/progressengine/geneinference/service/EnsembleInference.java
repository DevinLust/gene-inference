package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("ensemble")
public class EnsembleInference extends BaseInferenceEngine {
    protected final RelationshipService relationshipService;

    public EnsembleInference(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }


    private Map<GradePair, Double> multinomialJointScores(Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        Map<GradePair, Double> multinomialDistribution = new HashMap<>();
        for (Grade grade1 : Grade.values()) {
            for (Grade grade2 : Grade.values()) {
                GradePair gradePair = new GradePair(grade1, grade2);
                double multiScore = multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency);
                multinomialDistribution.put(gradePair, multiScore);
            }
        }
        return multinomialDistribution;
    }

    @Transactional
    // Updates the relationship with a new joint distribution using purely multinomial distributions
    public void findJointDistribution(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        for (Category category : Category.values()) {
            Map<Grade, Integer> phenotypeFrequency = relationship.getPhenotypeFrequencies(category);
            Grade phenotype1 = parent1.getPhenotype(category);
            Grade phenotype2 = parent2.getPhenotype(category);

            Map<GradePair, Double> intermediateScores = multinomialJointScores(phenotype1, phenotype2, phenotypeFrequency);
            normalizeScores(intermediateScores);
            relationship.setJointDistribution(category, intermediateScores);
        }
    }

    private Map<Grade, Double> findChildDistributionByCategory(Relationship relationship, Grade childPhenotype, Category category) {
        Map<Grade, Double> childHiddenDistribution = new EnumMap<>(Grade.class);

        // find the probability distribution of the child's hidden allele given a pair of assumed hidden alleles from the parents
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = findConditionalDistributions(relationship, childPhenotype, category);

        // get a true joint distribution by multiplying each joint probability by the respective marginals and normalizing
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);
        // sum all conditional distributions from each genotype multiplied by the joint probability of that genotype
        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double marginal1 = relationship.getParent1().getDistribution(category, DistributionType.INFERRED).get(gradePair.getFirst());
            double marginal2 = relationship.getParent2().getDistribution(category, DistributionType.INFERRED).get(gradePair.getSecond());
            double jointProbability = entry.getValue() * marginal1 * marginal2; // un-normalized
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
        // normalize the distribution
        normalizeScores(childHiddenDistribution);

        return childHiddenDistribution;
    }

    @Transactional
    // Returns a new Map of grades to probability given the relationship and observed phenotype of the child
    public void inferChildHiddenDistribution(Relationship relationship, Sheep child) {
        for (Category category : Category.values()) {
            Grade childPhenotype = child.getPhenotype(category);
            Map<Grade, Double> childHiddenDistribution = findChildDistributionByCategory(relationship, childPhenotype, category);

            child.setDistribution(category, DistributionType.PRIOR, childHiddenDistribution);
        }
    }

    // updates the hidden distributions of each parent in the relationship
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        // use the multinomial scores of the relationship each sheep is apart of
        for  (Category category : Category.values()) {
            Map<Grade, Double> parent1NewMarginalProbabilities = ensembleMarginalProbability(parent1, category);
            Map<Grade, Double> parent2NewMarginalProbabilities = ensembleMarginalProbability(parent2, category);

            parent1.setDistribution(category, DistributionType.INFERRED, parent1NewMarginalProbabilities);
            parent2.setDistribution(category, DistributionType.INFERRED, parent2NewMarginalProbabilities);
        }
    }

    private Map<Grade, Double> ensembleMarginalProbability(Sheep parent, Category category) {
        List<Relationship> allRelationships = relationshipService.findRelationshipsByParent(parent);
        List<Map<Grade, Double>> marginalProbabilities = new ArrayList<>();

        // get a list of all the marginals purely with multinomial score to keep it idempotent
        for (Relationship relationship : allRelationships) {
            boolean firstParent = relationship.getParent1().equals(parent);

            Map<Grade, Double> newMarginalProbabilities = completeJointContextMarginal(relationship, firstParent, category);

            fillMissingValuesWithZero(newMarginalProbabilities);
            marginalProbabilities.add(newMarginalProbabilities);
        }

        // combine the marginals across the relationships using product of experts
        Map<Grade, Double> ensembleProbabilities = marginalProbabilities.getFirst();
        for (int i = 1; i < marginalProbabilities.size(); i++) {
            productOfExperts(ensembleProbabilities, marginalProbabilities.get(i));
        }

        // if the sheep has a prior distribution then combine it
        Map<Grade, Double> priorProbabilities = parent.getDistribution(category, DistributionType.PRIOR);
        if (priorProbabilities != null && priorProbabilities.size() == Grade.values().length) {
            productOfExperts(ensembleProbabilities, priorProbabilities);
        }

        return ensembleProbabilities;
    }

    // accumulate the joint distribution multiplied by each corresponding marginal
    private Map<Grade, Double> completeJointContextMarginal(Relationship relationship, boolean firstParent, Category category) {
        Map<Grade, Double> partialMarginals = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);
        Map<Grade, Double> firstParentDistribution = relationship.getParent1().getDistribution(category, DistributionType.INFERRED);
        Map<Grade, Double> secondParentDistribution = relationship.getParent2().getDistribution(category, DistributionType.INFERRED);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double newProbability = entry.getValue() * firstParentDistribution.get(gradePair.getFirst()) * secondParentDistribution.get(gradePair.getSecond());

            // the probability contributes if the other parents marginal allows it
            if (firstParent) {
                partialMarginals.merge(gradePair.getFirst(), newProbability, Double::sum);
            } else {
                partialMarginals.merge(gradePair.getSecond(), newProbability, Double::sum);
            }
        }
        normalizeScores(partialMarginals);

        return partialMarginals;
    }
}
