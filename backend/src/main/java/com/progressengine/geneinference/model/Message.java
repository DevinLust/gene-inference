package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.SheepService;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Message {
    protected final Node<?> source;
    protected final Node<?> target;
    protected Map<Category, Map<String, Double>> distribution;

    public Message() {
        this.source = null;
        this.target = null;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            initializeUniform(category);
        }
    }

    public Message(Node<?> source, Node<?> target) {
        this.source = source;
        this.target = target;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            initializeUniform(category);
        }
    }

    public Message(Node<?> source, Node<?> target, Map<Category, Map<String, Double>> distribution) {
        this.source = source;
        this.target = target;
        this.distribution = distribution;
    }

    protected <A extends Enum<A> & Allele> void initializeUniform(Category category) {
        Map<A, Double> uniformDist = SheepService.createUniformDistribution(category);
        Map<String, Double> uniformStrDist = uniformDist.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().code(), 
                Map.Entry::getValue
            ));
        this.distribution.put(category, uniformStrDist);
    }

    public <A extends Enum<A> & Allele> Map<A, Double> getDistributionByCategory(Category category) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<String, Double> alleleCodeMap = distribution.get(category);
        if (alleleCodeMap == null) {
            return new EnumMap<>(domain.getAlleleType());
        }

        Map<A, Double> alleleMap = new EnumMap<>(domain.getAlleleType());
        for (Map.Entry<String, Double> entry : alleleCodeMap.entrySet()) {
            alleleMap.put(domain.parse(entry.getKey()), entry.getValue());
        }

        return alleleMap;
    }

    public Node<?> getSource() {
        return source;
    }

    public Node<?> getTarget() {
        return target;
    }

    public Map<Category, Map<String, Double>> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<Category, Map<String, Double>> distribution) {
        this.distribution =  distribution;
    }

    public <A extends Enum<A> & Allele> void setDistributionForCategory(Category category, Map<A, Double> distribution) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<A, Double> entry : distribution.entrySet()) {
            result.put(entry.getKey().code(), entry.getValue());
        }
        this.distribution.put(category, result);
    }

    abstract Map<Category, Map<String, Double>> computeMessage(List<Message> operands);

    abstract <A extends Enum<A> & Allele> Map<A, Double> computeMessageForCategory(Category category, List<Message> operands);
}
