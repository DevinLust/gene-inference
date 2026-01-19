package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheepMessage extends Message {
    private final Node<Sheep> source;
    private final Node<Relationship> target;

    public SheepMessage(Node<Sheep> source, Node<Relationship> target) {
        this.source = source;
        this.target = target;
        this.distribution = SheepService.createUniformDistribution();
    }

    @Override
    public Node<Sheep> getSource() {
        return source;
    }

    @Override
    public Node<Relationship> getTarget() {
        return target;
    }

    @Override
    public Map<Grade, Double> computeMessage(List<Message> messages) {
        Sheep sheep = source.getValue();
        Map<Grade, Double> distribution = sheep.getDistribution(Category.SWIM, DistributionType.PRIOR);

        for (Message message : messages) {
            Map<Grade, Double> messageDist = message.getDistribution();
            productOfExperts(distribution, messageDist);
        }

        return distribution;
    }

    private void productOfExperts(Map<Grade, Double> existingDistribution, Map<Grade, Double> newDistribution) {
        for (Map.Entry<Grade, Double> entry : existingDistribution.entrySet()) {
            double newProbability = newDistribution.get(entry.getKey());
            entry.setValue(entry.getValue() * newProbability);
        }

        normalizeScores(existingDistribution);
    }
}
