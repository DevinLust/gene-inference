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

        // TODO - refactor loopMarginalProbability to be more efficient in calculating common messages
        // loopy belief propagation to get new marginals
        for (Category category : Category.values()) {
            Map<Grade, Double> parent1NewMarginalProbabilities = loopMarginalProbability(parent1, parent1Relationships, parent2Relationships, relationship, parent2, category);
            Map<Grade, Double> parent2NewMarginalProbabilities = loopMarginalProbability(parent2, parent2Relationships, parent1Relationships, relationship, parent1, category);


            parent1.setDistribution(category, DistributionType.INFERRED, parent1NewMarginalProbabilities);
            parent2.setDistribution(category, DistributionType.INFERRED, parent2NewMarginalProbabilities);
        }
    }

    // Uses the principal of loopy belief propagation to pass messages from immediate partners to the relationship in question
    private Map<Grade, Double> loopMarginalProbability(Sheep parent, List<Relationship> relationships, List<Relationship> otherParentsRelationships, Relationship currentRelationship, Sheep otherParent, Category category) {
        // calculate the message to the current relationship
        Map<Grade, Double> messageToRelationship = otherParent.getDistribution(category, DistributionType.PRIOR);
        combineMessages(messageToRelationship, otherParentsRelationships, currentRelationship, otherParent, category);

        Map<Grade, Double> newMarginal = parent.getDistribution(category, DistributionType.PRIOR);
        productOfExperts(newMarginal, halfJointMarginal(currentRelationship, messageToRelationship, currentRelationship.getParent1().getId().equals(otherParent.getId()), category));

        // combine the message from this relationship with the other relationships for this parent
        combineMessages(newMarginal, relationships, currentRelationship, parent, category);

        return newMarginal;
    }

    private void combineMessages(Map<Grade, Double> overallMessage, List<Relationship> relationships, Relationship currentRelationship, Sheep currentParent, Category category) {
        for (Relationship relationship : relationships) {
            if (!relationship.getId().equals(currentRelationship.getId())) {
                Sheep secondaryParent = relationship.getParent1().getId().equals(currentParent.getId()) ? relationship.getParent2() : relationship.getParent1();
                boolean firstParent = relationship.getParent1().getId().equals(secondaryParent.getId()); // whether the secondary parent is the first or not
                productOfExperts(overallMessage, halfJointMarginal(relationship, secondaryParent.getDistribution(category, DistributionType.INFERRED), firstParent, category));
            }
        }
    }

    // Marginalizes the joint distribution using the specified weight distribution and which parent it relates to in the relationship, the score parent
    private Map<Grade, Double> halfJointMarginal(Relationship relationship, Map<Grade, Double> gradeDistribution, boolean firstParent, Category category) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double probability = entry.getValue();
            Grade scoreGrade = firstParent ? pair.getFirst() : pair.getSecond();
            Grade targetGrade = firstParent ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * gradeDistribution.get(scoreGrade), Double::sum);
        }
        normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
