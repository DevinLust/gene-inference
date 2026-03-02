package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class Message {
    protected final Node<?> source;
    protected final Node<?> target;
    protected Map<Category, Map<Grade, Double>> distribution;

    public Message() {
        this.source = null;
        this.target = null;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            this.distribution.put(category, SheepService.createUniformDistribution());
        }
    }

    public Message(Node<?> source, Node<?> target) {
        this.source = source;
        this.target = target;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            this.distribution.put(category, SheepService.createUniformDistribution());
        }
    }

    public Message(Node<?> source, Node<?> target, Map<Category, Map<Grade, Double>> distribution) {
        this.source = source;
        this.target = target;
        this.distribution = distribution;
    }

    public Node<?> getSource() {
        return source;
    }

    public Node<?> getTarget() {
        return target;
    }

    public Map<Category, Map<Grade, Double>> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<Category, Map<Grade, Double>> distribution) {
        this.distribution =  distribution;
    }

    abstract Map<Category, Map<Grade, Double>> computeMessage(List<Message> operands);
}
