package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service("loopy")
public class LoopyInference extends EnsembleInference {

    public LoopyInference(RelationshipService relationshipService) {
        super(relationshipService);
    }
    
    @Override
    public void updateMarginalProbabilities(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        // List of relationships for each parent
        List<Relationship> parent1Relationships = relationshipService.findRelationshipsByParent(parent1);
        List<Relationship> parent2Relationships = relationshipService.findRelationshipsByParent(parent2);

        // loopy belief propagation to get new marginals
        for (Category category : Category.values()) {
            List<Map<Grade, Double>> newMarginals = loopMarginalProbabilities(relationship, parent1Relationships, parent2Relationships, category);

            parent1.setDistribution(category, DistributionType.INFERRED, newMarginals.get(0));
            parent2.setDistribution(category, DistributionType.INFERRED, newMarginals.get(1));
        }
    }

    // Uses the principal of loopy belief propagation to pass messages from immediate partners to the relationship in question
    private List<Map<Grade, Double>> loopMarginalProbabilities(Relationship currentRelationship, List<Relationship> parent1Relationships, List<Relationship> parent2Relationships, Category category) {
        Sheep parent1 = currentRelationship.getParent1();
        Sheep parent2 = currentRelationship.getParent2();

        Map<Grade, Double> parent1MessageToRelationship = parent1.getDistribution(category, DistributionType.PRIOR);
        combineMessages(parent1MessageToRelationship, parent1Relationships, currentRelationship, parent1, category);

        Map<Grade, Double> parent2MessageToRelationship = parent2.getDistribution(category, DistributionType.PRIOR);
        combineMessages(parent2MessageToRelationship, parent2Relationships, currentRelationship, parent2, category);

        // parent1 belief
        Map<Grade, Double> parent1Belief = new EnumMap<>(parent1MessageToRelationship);
        productOfExperts(parent1Belief, halfJointMarginal(currentRelationship, parent2MessageToRelationship, false, category));

        // parent2 belief
        Map<Grade, Double> parent2Belief = new EnumMap<>(parent2MessageToRelationship);
        productOfExperts(parent2Belief, halfJointMarginal(currentRelationship, parent1MessageToRelationship, true, category));

        return List.of(parent1Belief, parent2Belief);
    }

    // combines the incoming messages from all relationships except the current relationship
    private void combineMessages(Map<Grade, Double> overallMessage, List<Relationship> relationships, Relationship currentRelationship, Sheep currentParent, Category category) {
        for (Relationship relationship : relationships) {
            if (!relationship.getId().equals(currentRelationship.getId())) {
                Sheep secondaryParent = relationship.getParent1().getId().equals(currentParent.getId()) ? relationship.getParent2() : relationship.getParent1();
                boolean firstParent = relationship.getParent1().getId().equals(secondaryParent.getId()); // whether the secondary parent is the first or not
                productOfExperts(overallMessage, halfJointMarginal(relationship, secondaryParent.getDistribution(category, DistributionType.INFERRED), firstParent, category));
            }
        }
    }

    // Marginalizes the joint distribution using the specified weight distribution and which parent it relates to in the relationship, the weighted parent
    private Map<Grade, Double> halfJointMarginal(Relationship relationship, Map<Grade, Double> gradeDistribution, boolean firstParent, Category category) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double probability = entry.getValue();
            Grade weightGrade = firstParent ? pair.getFirst() : pair.getSecond();
            Grade targetGrade = firstParent ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * gradeDistribution.get(weightGrade), Double::sum);
        }
        normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
