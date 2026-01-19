package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;

import java.util.List;
import java.util.Map;

public abstract class Message {
    protected final Node<?> source;
    protected final Node<?> target;
    protected Map<Grade, Double> distribution;

    public Message() {
        this.source = null;
        this.target = null;
        this.distribution = SheepService.createUniformDistribution();
    }

    public Message(Node<?> source, Node<?> target) {
        this.source = source;
        this.target = target;
        this.distribution = SheepService.createUniformDistribution();
    }

    public Message(Node<?> source, Node<?> target, Map<Grade, Double> distribution) {
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

    public Map<Grade, Double> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<Grade, Double> distribution) {
        this.distribution =  distribution;
    }

    protected <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }

    abstract Map<Grade, Double> computeMessage(List<Message> operands);
}
