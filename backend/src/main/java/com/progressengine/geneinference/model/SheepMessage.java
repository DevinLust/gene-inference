package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SheepMessage extends Message {
    private final Node<Sheep> source;
    private final Node<Relationship> target;

    public SheepMessage(Node<Sheep> source, Node<Relationship> target) {
        this.source = source;
        this.target = target;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            this.distribution.put(category, SheepService.createUniformDistribution());
        }
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
    public Map<Category, Map<Grade, Double>> computeMessage(List<Message> messages) {
        Map<Category, Map<Grade, Double>> distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            distribution.put(category, SheepService.createUniformDistribution());
        }

        for (Message message : messages) {
            Map<Category, Map<Grade, Double>> messageDist = message.getDistribution();
            for (Category category : Category.values()) {
                InferenceMath.productOfExperts(distribution.get(category), messageDist.get(category));
            }
        }

        return distribution;
    }

    @Override
    public Map<Grade, Double> computeMessageForCategory(Category category, List<Message> messages) {
        Map<Grade, Double> distribution = SheepService.createUniformDistribution();

        for (Message message : messages) {
            InferenceMath.productOfExperts(distribution, message.getDistribution().get(category));
        }

        return distribution;
    }
}
