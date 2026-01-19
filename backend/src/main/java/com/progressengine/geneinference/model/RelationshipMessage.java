package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RelationshipMessage extends Message {
    private final Node<Relationship> source;
    private final Node<Sheep> target;

    public RelationshipMessage(Node<Relationship> source, Node<Sheep> target) {
        this.source = source;
        this.target = target;
        this.distribution = SheepService.createUniformDistribution();
    }

    @Override
    public Node<Relationship> getSource() {
        return source;
    }

    @Override
    public Node<Sheep> getTarget() {
        return target;
    }

    @Override
    public Map<Grade, Double> computeMessage(List<Message> messages) {
        Relationship relationship = source.getValue();
        Sheep targetSheep = target.getValue();
        boolean firstParentAsWeight = relationship.getParent2().equals(targetSheep);
        Map<Grade, Double> weightDistribution = messages.getFirst().getDistribution();

        Map<Grade, Double> result = halfJointMarginal(relationship, weightDistribution, firstParentAsWeight, Category.SWIM);
        return result;
    }

    private Map<Grade, Double> halfJointMarginal(Relationship relationship, Map<Grade, Double> weightDistribution, boolean firstParentAsWeight, Category category) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double probability = entry.getValue();
            Grade weightGrade = firstParentAsWeight ? pair.getFirst() : pair.getSecond();
            Grade targetGrade = firstParentAsWeight ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * weightDistribution.get(weightGrade), Double::sum);
        }
        normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
