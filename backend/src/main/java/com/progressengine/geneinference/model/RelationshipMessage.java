package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationshipMessage extends Message {
    private final Node<Relationship> source;
    private final Node<Sheep> target;

    public RelationshipMessage() {
        this.source = null;
        this.target = null;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            initializeUniform(category);
        }
    }

    public RelationshipMessage(Node<Relationship> source, Node<Sheep> target) {
        this.source = source;
        this.target = target;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            initializeUniform(category);
        }
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
    public Map<Category, Map<String, Double>> computeMessage(List<Message> messages) {
        Map<Category, Map<String, Double>> result = new EnumMap<>(Category.class);

        for (Category category : Category.values()) {
            result.put(category, computeMessageForCategoryAsCodes(category, messages));
        }

        return result;
    }

    private <A extends Enum<A> & Allele> Map<String, Double> computeMessageForCategoryAsCodes(
            Category category,
            List<Message> messages
    ) {
        Map<A, Double> typed = computeMessageForCategory(category, messages);
        return toCodeDistribution(typed);
    }

    private <A extends Enum<A> & Allele> Map<String, Double> toCodeDistribution(Map<A, Double> distribution) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<A, Double> entry : distribution.entrySet()) {
            result.put(entry.getKey().code(), entry.getValue());
        }
        return result;
    }

    @Override
    public <A extends Enum<A> & Allele> Map<A, Double> computeMessageForCategory(Category category, List<Message> messages) {
        Relationship relationship = source.getValue();
        Sheep targetSheep = target.getValue();

        boolean firstParentAsWeight = relationship.getParent2().equals(targetSheep);
        Map<A, Double> weightDistribution = messages.getFirst().getDistributionByCategory(category);
        Map<AllelePair<A>, Double> jointDistribution = relationship.getJointDistribution(category);

        for (int i = 1; i < messages.size(); i++) {
            incorporateChildMessageForCategory(category, jointDistribution, messages.get(i));
        }

        return halfJointMarginal(jointDistribution, weightDistribution, firstParentAsWeight, category);

    }

    protected <A extends Enum<A> & Allele> void incorporateChildMessageForCategory(
            Category category,
            Map<AllelePair<A>, Double> jointDistribution,
            Message message
    ) {
        Sheep child = (Sheep) message.getSource().getValue();

        if (child.getBirthRecord() == null) {
            return;
        }

        Map<Category, PhenotypeAtBirth> phenotypesAtBirth =
                child.getBirthRecord().getPhenotypesAtBirthOrganized();

        PhenotypeAtBirth phenotypeAtBirth = phenotypesAtBirth.get(category);

        if (phenotypeAtBirth == null) {
            return; // no evidence for this category
        }

        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> childDistribution = message.getDistributionByCategory(category);

        A childPhenotype = domain.parse(phenotypeAtBirth.childCode());
        A phenotype1 = domain.parse(phenotypeAtBirth.parent1Code());
        A phenotype2 = domain.parse(phenotypeAtBirth.parent2Code());

        for (Map.Entry<AllelePair<A>, Double> entry : jointDistribution.entrySet()) {
            AllelePair<A> pair = entry.getKey();
            double jointProb = entry.getValue();

            Map<A, Double> expectedChildHidden =
                    InferenceMath.childHiddenDistributionGivenParents(
                            pair,
                            phenotype1,
                            phenotype2,
                            childPhenotype,
                            domain
                    );

            double scalingFactor = 0.0;
            for (A allele : domain.getAlleles()) {
                scalingFactor += expectedChildHidden.getOrDefault(allele, 0.0)
                        * childDistribution.getOrDefault(allele, 0.0);
            }

            entry.setValue(jointProb * scalingFactor);
        }

        InferenceMath.normalizeScores(jointDistribution);
    }

    private <A extends Enum<A> & Allele> Map<A, Double> halfJointMarginal(
        Map<AllelePair<A>, Double> jointDistribution, 
        Map<A, Double> weightDistribution, 
        boolean firstParentAsWeight,
        Category category
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> newHalfMarginal = new EnumMap<>(domain.getAlleleType());

        for (Map.Entry<AllelePair<A>, Double> entry : jointDistribution.entrySet()) {
            AllelePair<A> pair = entry.getKey();
            double probability = entry.getValue();
            A weightGrade = firstParentAsWeight ? pair.getFirst() : pair.getSecond();
            A targetGrade = firstParentAsWeight ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * weightDistribution.get(weightGrade), Double::sum);
        }
        InferenceMath.normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
