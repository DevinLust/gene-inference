package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
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
        Map<Grade, Double> parent1NewMarginalProbabilities = loopMarginalProbability(parent1, parent1Relationships, parent2Relationships, relationship, parent2);
        Map<Grade, Double> parent2NewMarginalProbabilities = loopMarginalProbability(parent2, parent2Relationships, parent1Relationships, relationship, parent1);


        parent1.setHiddenDistribution(parent1NewMarginalProbabilities);
        parent2.setHiddenDistribution(parent2NewMarginalProbabilities);
    }

    // TODO - parents forget their own previous relationships
    private Map<Grade, Double> loopMarginalProbability(Sheep parent, List<Relationship> relationships, List<Relationship> otherParentsRelationships, Relationship currentRelationship, Sheep otherParent) {
        // calculate the message to the current relationship
        Map<Grade, Double> messageToRelationship = otherParent.getPriorDistribution() != null && !otherParent.getPriorDistribution().isEmpty() ? new EnumMap<>(otherParent.getPriorDistribution()) : SheepService.createUniformDistribution();
        for (Relationship relationship : otherParentsRelationships) {
            if (!relationship.getId().equals(currentRelationship.getId())) {
                Sheep secondaryParent = relationship.getParent1().getId().equals(otherParent.getId()) ? relationship.getParent2() : relationship.getParent1();
                boolean firstParent = relationship.getParent1().getId().equals(secondaryParent.getId()); // whether the secondary parent is the first or not
                productOfExperts(messageToRelationship, halfJointMarginal(relationship, secondaryParent.getHiddenDistribution(), firstParent));
            }
        }

        Map<Grade, Double> newMarginal = parent.getPriorDistribution() != null && !parent.getPriorDistribution().isEmpty() ? new EnumMap<>(parent.getPriorDistribution()) : SheepService.createUniformDistribution();
        productOfExperts(newMarginal, halfJointMarginal(currentRelationship, messageToRelationship, currentRelationship.getParent1().getId().equals(otherParent.getId())));

        // combine the message from this relationship with the other relationships for this parent
        for (Relationship relationship : relationships) {
            if (!relationship.getId().equals(currentRelationship.getId())) {
                Sheep secondaryParent = relationship.getParent1().getId().equals(parent.getId()) ? relationship.getParent2() : relationship.getParent1();
                boolean firstParent = relationship.getParent1().getId().equals(secondaryParent.getId()); // whether the secondary parent is the first or not
                productOfExperts(newMarginal, halfJointMarginal(relationship, secondaryParent.getHiddenDistribution(), firstParent));
            }
        }

        return newMarginal;
    }

    // Marginalizes the joint distribution using the specified weight distribution and which parent it relates to in the relationship, the score parent
    private Map<Grade, Double> halfJointMarginal(Relationship relationship, Map<Grade, Double> gradeDistribution, boolean firstParent) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();

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
