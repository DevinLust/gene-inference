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

    protected <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }

    // Returns the probability the given allele came from each parent given the assumed hidden alleles
    protected double[] probabilityAlleleFromParents(GradePair hiddenAlleles, Grade phenotype1, Grade phenotype2, Grade allele) {
        double[] probabilities = new double[2];

        double totalProbabilityOfAllele = 0.0;
        double probabilityOfAlleleGivenParent1 = 0.0;
        if (phenotype1.equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (hiddenAlleles.getFirst().equals(allele)) {
            probabilityOfAlleleGivenParent1 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        double probabilityOfAlleleGivenParent2 = 0.0;
        if (phenotype2.equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }
        if (hiddenAlleles.getSecond().equals(allele)) {
            probabilityOfAlleleGivenParent2 += 0.5;
            totalProbabilityOfAllele += 0.25;
        }

        if (totalProbabilityOfAllele == 0) {
            return probabilities;
        }

        // multiply each ratio by 0.5 as the probability that each parent is chosen
        probabilities[0] = (0.5 * probabilityOfAlleleGivenParent1) /  totalProbabilityOfAllele;
        probabilities[1] = (0.5 * probabilityOfAlleleGivenParent2) /  totalProbabilityOfAllele;
        return probabilities;
    }

    abstract Map<Category, Map<Grade, Double>> computeMessage(List<Message> operands);
}
