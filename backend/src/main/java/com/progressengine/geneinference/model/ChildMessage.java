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

public class ChildMessage extends RelationshipMessage {
    private final Node<Relationship> source;
    private final Node<Sheep> target;

    public ChildMessage(Node<Relationship> source, Node<Sheep> target) {
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
        Relationship relationship = source.getValue();
        Sheep child = target.getValue();
        Message parent1Message = messages.get(0);
        Message parent2Message = messages.get(1);

        Map<Category, Map<AlleleCodePair, Double>> jointDistributions = relationship.getJointDistributions();

        // incorporate the parent messages into the joint distribution
        incorporateParents(jointDistributions, parent1Message, parent2Message);

        // incorporate the sibling messages into the joint distribution
        for (Message siblingMessage : messages.subList(2, messages.size())) {
            incorporateChildMessageAsCodes(jointDistributions, siblingMessage);
        }

        // add the conditional distributions of seeing the child phenotype
        return accumulateConditionals(jointDistributions, child);
    }

    private void incorporateChildMessageAsCodes(
        Map<Category, Map<AlleleCodePair, Double>> jointDistributions,
        Message message
    ) {
        for (Category category : Category.values()) {
            incorporateChildMessageForCategoryAsCodes(category, jointDistributions, message);
        }
    }

    private <A extends Enum<A> & Allele> void incorporateChildMessageForCategoryAsCodes(
        Category category,
        Map<Category, Map<AlleleCodePair, Double>> jointDistributions,
        Message message
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        Map<AlleleCodePair, Double> codedJoint = jointDistributions.get(category);
        Map<AllelePair<A>, Double> typedJoint = new HashMap<>();

        for (Map.Entry<AlleleCodePair, Double> entry : codedJoint.entrySet()) {
            typedJoint.put(entry.getKey().toAllelePair(domain), entry.getValue());
        }

        incorporateChildMessageForCategory(category, typedJoint, message);

        Map<AlleleCodePair, Double> updatedCodedJoint = new HashMap<>();
        for (Map.Entry<AllelePair<A>, Double> entry : typedJoint.entrySet()) {
            updatedCodedJoint.put(AlleleCodePair.fromAllelePair(entry.getKey()), entry.getValue());
        }

        jointDistributions.put(category, updatedCodedJoint);
    }

    @Override
    public <A extends Enum<A> & Allele> Map<A, Double> computeMessageForCategory(Category category, List<Message> messages) {
        Relationship relationship = source.getValue();
        Sheep child = target.getValue();

        Message parent1Message = messages.get(0);
        Message parent2Message = messages.get(1);

        Map<AllelePair<A>, Double> jointDistribution = relationship.getJointDistribution(category);

        incorporateParentsForCategory(category, jointDistribution, parent1Message, parent2Message);

        for (Message siblingMessage : messages.subList(2, messages.size())) {
            incorporateChildMessageForCategory(category, jointDistribution, siblingMessage);
        }

        return accumulateConditionalsForCategory(category, jointDistribution, child);
    }

    private void incorporateParents(
        Map<Category, Map<AlleleCodePair, Double>> jointDistributions,
        Message parent1Message,
        Message parent2Message
    ) {
        for (Category category : Category.values()) {
            incorporateParentsForCategoryAsCodes(category, jointDistributions, parent1Message, parent2Message);
        }
    }

    private <A extends Enum<A> & Allele> void incorporateParentsForCategoryAsCodes(
            Category category,
            Map<Category, Map<AlleleCodePair, Double>> jointDistributions,
            Message parent1Message,
            Message parent2Message
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        Map<AlleleCodePair, Double> codedJoint = jointDistributions.get(category);
        Map<AllelePair<A>, Double> typedJoint = new HashMap<>();

        for (Map.Entry<AlleleCodePair, Double> entry : codedJoint.entrySet()) {
            typedJoint.put(entry.getKey().toAllelePair(domain), entry.getValue());
        }

        incorporateParentsForCategory(category, typedJoint, parent1Message, parent2Message);

        Map<AlleleCodePair, Double> updatedCodedJoint = new HashMap<>();
        for (Map.Entry<AllelePair<A>, Double> entry : typedJoint.entrySet()) {
            updatedCodedJoint.put(AlleleCodePair.fromAllelePair(entry.getKey()), entry.getValue());
        }

        jointDistributions.put(category, updatedCodedJoint);
    }

    private <A extends Enum<A> & Allele> void incorporateParentsForCategory(Category category, Map<AllelePair<A>, Double> jointDistribution, Message parent1Message, Message parent2Message) {
        Map<A, Double> parent1Dist = parent1Message.getDistributionByCategory(category);
        Map<A, Double> parent2Dist = parent2Message.getDistributionByCategory(category);
        for (Map.Entry<AllelePair<A>, Double> entry : jointDistribution.entrySet()) {
            AllelePair<A> pair = entry.getKey();
            Double oldProb = entry.getValue();
            entry.setValue(oldProb * parent1Dist.get(pair.getFirst()) *  parent2Dist.get(pair.getSecond()));
        }
        InferenceMath.normalizeScores(jointDistribution);
    }

    // Iterates through possible hidden pairs and weighting and accumulating their distributions
    private Map<Category, Map<String, Double>> accumulateConditionals(
        Map<Category, Map<AlleleCodePair, Double>> jointDistributions,
        Sheep child
    ) {
        Map<Category, Map<String, Double>> result = new EnumMap<>(Category.class);

        for (Category category : Category.values()) {
            result.put(
                    category,
                    accumulateConditionalsForCategoryAsCodes(category, jointDistributions.get(category), child)
            );
        }

        return result;
    }

    private <A extends Enum<A> & Allele> Map<String, Double> accumulateConditionalsForCategoryAsCodes(
            Category category,
            Map<AlleleCodePair, Double> codedJointDistribution,
            Sheep child
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        Map<AllelePair<A>, Double> typedJointDistribution = new HashMap<>();
        for (Map.Entry<AlleleCodePair, Double> entry : codedJointDistribution.entrySet()) {
            typedJointDistribution.put(entry.getKey().toAllelePair(domain), entry.getValue());
        }

        Map<A, Double> typedResult =
                accumulateConditionalsForCategory(category, typedJointDistribution, child);

        Map<String, Double> codedResult = new HashMap<>();
        for (Map.Entry<A, Double> entry : typedResult.entrySet()) {
            codedResult.put(entry.getKey().code(), entry.getValue());
        }

        return codedResult;
    }

    private <A extends Enum<A> & Allele> Map<A, Double> accumulateConditionalsForCategory(
            Category category,
            Map<AllelePair<A>, Double> jointDistribution,
            Sheep child
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);
        Map<A, Double> result = new EnumMap<>(domain.getAlleleType());
        fillMissingWithZero(result, domain);

        Map<Category, PhenotypeAtBirth> phenotypesAtBirth =
                child.getBirthRecord().getPhenotypesAtBirthOrganized();

        A childPhenotype = domain.parse(phenotypesAtBirth.get(category).childCode());
        A parent1Phenotype = domain.parse(phenotypesAtBirth.get(category).parent1Code());
        A parent2Phenotype = domain.parse(phenotypesAtBirth.get(category).parent2Code());

        for (Map.Entry<AllelePair<A>, Double> entry : jointDistribution.entrySet()) {
            AllelePair<A> pair = entry.getKey();
            double jointWeight = entry.getValue();

            Map<A, Double> conditionalDist =
                    InferenceMath.childHiddenDistributionGivenParents(
                            pair,
                            parent1Phenotype,
                            parent2Phenotype,
                            childPhenotype,
                            domain
                    );

            addDistributions(result, conditionalDist, jointWeight);
        }

        InferenceMath.normalizeScores(result);
        return result;
    }

    private <A extends Enum<A> & Allele> void addDistributions(Map<A, Double> distribution, Map<A, Double> weightedDist, double weight) {
        for (Map.Entry<A, Double> entry : distribution.entrySet()) {
            entry.setValue(entry.getValue() + weight * weightedDist.get(entry.getKey()));
        }
    }

    private <A extends Enum<A> & Allele> void fillMissingWithZero(Map<A, Double> distribution, AlleleDomain<A> domain) {
        for (A allele : domain.getAlleles()) {
            distribution.putIfAbsent(allele, 0.0);
        }
    }
}
