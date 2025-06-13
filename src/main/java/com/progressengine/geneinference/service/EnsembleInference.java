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

    // TODO - need to change the way prior child distribution is stored since its dependent on observed phenotype and not actually shared among all children
    // Returns a new Map of grades to probability given the relationship and observed phenotype of the child
    public void inferChildHiddenDistribution(Relationship relationship, Sheep child) {
        Map<Grade, Double> childHiddenDistribution = new EnumMap<>(Grade.class);
        Grade childPhenotype = child.getPhenotype();

        // find the probability distribution of the hidden allele given both genotypes
        Map<GradePair, Map<Grade, Double>> conditionalDistributions = findConditionalDistributions(relationship, childPhenotype);

        // get a true joint distribution by multiplying each joint probability by the respective marginals and normalizing
        Map<GradePair, Double> jointDistribution = new HashMap<>(relationship.getHiddenPairsDistribution());
        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double marginal1 = relationship.getParent1().getHiddenDistribution().get(gradePair.getFirst());
            double marginal2 = relationship.getParent2().getHiddenDistribution().get(gradePair.getSecond());
            entry.setValue(entry.getValue() * marginal1 * marginal2);
        }
        normalizeScores(jointDistribution);

        // sum all conditional distributions from each genotype multiplied by the joint probability of that genotype
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
        fillMissingValuesWithZero(childHiddenDistribution);

        child.setPriorDistribution(childHiddenDistribution);
    }

    // TODO - parents can sometimes lose context from previous relationship ex. (B, B) & (A, S). P2 should guarantee S but loses context in other relationships (assuming P1 knows it can't have an S)
    // TODO - low data marginals are a bit extreme due to product of experts
    // updates the hidden distributions of each parent in the relationship
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        Map<Grade, Double> parent1NewMarginalProbabilities = ensembleMarginalProbability(parent1);
        Map<Grade, Double> parent2NewMarginalProbabilities = ensembleMarginalProbability(parent2);

        // experiment: want to try and use each other to update each other

        combineMarginalsWithJointContext(parent1NewMarginalProbabilities, parent2NewMarginalProbabilities, relationship.getHiddenPairsDistribution());

        // ---------------------------------------------------------------

        parent1.setHiddenDistribution(parent1NewMarginalProbabilities);
        parent2.setHiddenDistribution(parent2NewMarginalProbabilities);
    }

    private Map<Grade, Double> ensembleMarginalProbability(Sheep parent) {
        List<Relationship> allRelationships = relationshipService.findRelationshipsByParent(parent);
        List<Map<Grade, Double>> marginalProbabilities = new ArrayList<>();

        for (Relationship relationship : allRelationships) {
            boolean firstParent = relationship.getParent1().equals(parent);

            Map<Grade, Double> newMarginalProbabilities = qualifiedJointContextMarginal(relationship, firstParent);

            fillMissingValuesWithZero(newMarginalProbabilities);
            marginalProbabilities.add(newMarginalProbabilities);
        }

        Map<Grade, Double> ensembleProbabilities = marginalProbabilities.getFirst();
        for (int i = 1; i < marginalProbabilities.size(); i++) {
            productOfExperts(ensembleProbabilities, marginalProbabilities.get(i));
        }

        Map<Grade, Double> priorProbabilities = parent.getPriorDistribution();
        if (priorProbabilities != null && priorProbabilities.size() == Grade.values().length) {
            productOfExperts(ensembleProbabilities, parent.getPriorDistribution());
        }

        return ensembleProbabilities;
    }

    // combines the probability of each marginal with the context of them occurring together in the joint
    private void combineMarginalsWithJointContext(Map<Grade, Double> parent1Marginals, Map<Grade, Double> parent2Marginals, Map<GradePair, Double> jointDistribution) {
        Map<Grade, Double> tempParent1Marginals = new EnumMap<>(Grade.class);
        Map<Grade, Double> tempParent2Marginals = new EnumMap<>(Grade.class);
        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double score = entry.getValue() * parent1Marginals.get(gradePair.getFirst()) * parent2Marginals.get(gradePair.getSecond());
            tempParent1Marginals.merge(gradePair.getFirst(), score, Double::sum);
            tempParent2Marginals.merge(gradePair.getSecond(), score, Double::sum);
        }
        normalizeScores(tempParent1Marginals);
        normalizeScores(tempParent2Marginals);

        // copy the new contextualized marginals back over
        for (Map.Entry<Grade, Double> entry : parent1Marginals.entrySet()) {
            entry.setValue(tempParent1Marginals.get(entry.getKey()));
        }
        for (Map.Entry<Grade, Double> entry : parent2Marginals.entrySet()) {
            entry.setValue(tempParent2Marginals.get(entry.getKey()));
        }
    }

    // builds a marginal distribution purely from the multinomial probabilities of the jointDistribution for the specified parent
    private Map<Grade, Double> noJointContextMarginal(Map<GradePair, Double> jointDistribution, boolean firstParent) {
        Map<Grade, Double> partialMarginals = new EnumMap<>(Grade.class);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double newProbability = entry.getValue();
            if (firstParent) {
                partialMarginals.merge(gradePair.getFirst(), newProbability, Double::sum);
            } else {
                partialMarginals.merge(gradePair.getSecond(), newProbability, Double::sum);
            }
        }

        return partialMarginals;
    }

    private Map<Grade, Double> qualifiedJointContextMarginal(Relationship relationship, boolean firstParent) {
        Map<Grade, Double> partialMarginals = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();
        Map<Grade, Double> qualifiedMarginal = firstParent ? relationship.getParent2().getHiddenDistribution() : relationship.getParent1().getHiddenDistribution();

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair gradePair = entry.getKey();
            double newProbability = entry.getValue();
            if (firstParent && qualifiedMarginal.get(gradePair.getSecond()) > 0.0) {
                partialMarginals.merge(gradePair.getFirst(), newProbability, Double::sum);
            } else if (qualifiedMarginal.get(gradePair.getFirst()) > 0.0) {
                partialMarginals.merge(gradePair.getSecond(), newProbability, Double::sum);
            }
        }
        normalizeScores(partialMarginals);

        return partialMarginals;
    }
}
