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

@Service("loopy")
public class LoopyInference extends EnsembleInference {

    public LoopyInference(RelationshipService relationshipService) {
        super(relationshipService);
    }

    /**
     * Updates the two sheep's marginal probabilities based on data from their relationship.
     *
     * @param relationship - the Relationship of the parents to update
     */
    @Transactional
    @Override
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        // List of relationships for each parent
        // TODO - filtering breaks tests because it can't mock it without overriding the tests
        // List<List<Relationship>> filteredRelationships = relationshipService.filterRelationshipsByParent(parent1, parent2, 5);
        // List<Relationship> parent1Relationships = filteredRelationships.get(0);
        // List<Relationship> parent2Relationships = filteredRelationships.get(1);
        List<Relationship> parent1Relationships = relationshipService.findRelationshipsByParent(parent1);
        List<Relationship> parent2Relationships = relationshipService.findRelationshipsByParent(parent2);

        // loopy belief propagation to get new marginals
        for (Category category : Category.values()) {
            List<Map<Grade, Double>> newMarginals = loopMarginalProbabilities(relationship, parent1Relationships, parent2Relationships, category);

            parent1.setDistribution(category, DistributionType.INFERRED, newMarginals.get(0));
            parent2.setDistribution(category, DistributionType.INFERRED, newMarginals.get(1));

            checkCertainty(parent1, category);
            checkCertainty(parent2, category);
        }
    }

    /**
     * Returns List containing the updated distributions of the two parents in the current
     * Relationship. Uses the principal of loopy belief propagation to pass messages from
     * immediate partners to the relationship and category in question. The final belief
     * is a product of all messages each parent gets from their Relationships.
     *
     * @param currentRelationship - the Relationship of the two parents to update
     * @param parent1Relationships - a List of Relationships the first parent is in
     * @param parent2Relationships - a List of Relationships the second parent is in
     * @param category - the Category of the distribution to update
     * @return a List containing the two updated and detached distributions of the parents
     * for the particular category
     */
    private List<Map<Grade, Double>> loopMarginalProbabilities(Relationship currentRelationship, List<Relationship> parent1Relationships, List<Relationship> parent2Relationships, Category category) {
        Sheep parent1 = currentRelationship.getParent1();
        Sheep parent2 = currentRelationship.getParent2();

        Map<Grade, Double> parent1MessageToRelationship = parent1.getDistribution(category, DistributionType.PRIOR);
        combineMessages(parent1MessageToRelationship, parent1Relationships, currentRelationship, parent1, category);

        Map<Grade, Double> parent2MessageToRelationship = parent2.getDistribution(category, DistributionType.PRIOR);
        combineMessages(parent2MessageToRelationship, parent2Relationships, currentRelationship, parent2, category);

        // parent1 belief
        Map<Grade, Double> parent1Belief = new EnumMap<>(parent1MessageToRelationship);
        InferenceMath.productOfExperts(parent1Belief, halfJointMarginal(currentRelationship, parent2MessageToRelationship, false, category));

        // parent2 belief
        Map<Grade, Double> parent2Belief = new EnumMap<>(parent2MessageToRelationship);
        InferenceMath.productOfExperts(parent2Belief, halfJointMarginal(currentRelationship, parent1MessageToRelationship, true, category));

        return List.of(parent1Belief, parent2Belief);
    }

    /**
     * Combines the incoming messages from all neighboring relationships except the
     * current relationship. This keeps the message independent of the other parent.
     * OverallMessage will be the message currentParent has about currentRelationship.
     *
     * @param overallMessage - the running product of messages
     * @param relationships - a List of Relationships of the current parent
     * @param currentRelationship - a reference to the current Relationship to be avoided
     * @param currentParent - a reference to the current parent, a Sheep
     * @param category - the target Category of this message
     */
    private void combineMessages(Map<Grade, Double> overallMessage, List<Relationship> relationships, Relationship currentRelationship, Sheep currentParent, Category category) {
        for (Relationship relationship : relationships) {
            if (!relationship.getId().equals(currentRelationship.getId())) {
                Sheep secondaryParent = relationship.getParent1().getId().equals(currentParent.getId()) ? relationship.getParent2() : relationship.getParent1();
                boolean firstParent = relationship.getParent1().getId().equals(secondaryParent.getId()); // whether the secondary parent is the first or not
                InferenceMath.productOfExperts(overallMessage, halfJointMarginal(relationship, secondaryParent.getDistribution(category, DistributionType.INFERRED), firstParent, category));
            }
        }
    }

    /**
     * Calculates the message the given relationship has about the other parent in
     * the Relationship. The specified parent is the parent used in the weighting
     * of the joint distribution which is marginalized over the other grade.
     *
     * @param relationship - the Relationship to pass a message from
     * @param weightDistribution - a distribution that is used to weight the
     *                           marginalization of the joint distribution
     * @param firstParentAsWeight - boolean to determine whether to use the first
     *                            parent in the relationship as the weight parent
     * @param category - the target Category of this message
     * @return a distribution that represents message from the given Relationship
     * to the other parent
     */
    private Map<Grade, Double> halfJointMarginal(Relationship relationship, Map<Grade, Double> weightDistribution, boolean firstParentAsWeight, Category category) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistributionsExperimental().get(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double probability = entry.getValue();
            Grade weightGrade = firstParentAsWeight ? pair.getFirst() : pair.getSecond();
            Grade targetGrade = firstParentAsWeight ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * weightDistribution.get(weightGrade), Double::sum);
        }
        InferenceMath.normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
