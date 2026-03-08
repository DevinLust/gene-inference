package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;

import java.util.EnumMap;
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
            this.distribution.put(category, SheepService.createUniformDistribution());
        }
    }

    public RelationshipMessage(Node<Relationship> source, Node<Sheep> target) {
        this.source = source;
        this.target = target;
        this.distribution = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            this.distribution.put(category, SheepService.createUniformDistribution());
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
    public Map<Category, Map<Grade, Double>> computeMessage(List<Message> messages) {
        Relationship relationship = source.getValue();
        Sheep targetSheep = target.getValue();

        boolean firstParentAsWeight = relationship.getParent2().equals(targetSheep);
        Map<Category, Map<Grade, Double>> weightDistribution = messages.getFirst().getDistribution();
        Map<Category, Map<GradePair, Double>> jointDistributions = relationship.getJointDistributions();

        // need to incorporate child messages into the joint before marginalization
        for (int i = 1; i < messages.size(); i++) {
            incorporateChildMessage(jointDistributions, messages.get(i));
        }

        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, halfJointMarginal(jointDistributions.get(category), weightDistribution.get(category), firstParentAsWeight));
        }
        return result;
    }

    @Override
    public Map<Grade, Double> computeMessageForCategory(Category category, List<Message> messages) {
        Relationship relationship = source.getValue();
        Sheep targetSheep = target.getValue();

        boolean firstParentAsWeight = relationship.getParent2().equals(targetSheep);
        Map<Grade, Double> weightDistribution = messages.get(0).getDistribution().get(category);
        Map<GradePair, Double> jointDistribution = relationship.getJointDistributions().get(category);

        for (int i = 1; i < messages.size(); i++) {
            incorporateChildMessageForCategory(category, jointDistribution, messages.get(i));
        }

        return halfJointMarginal(jointDistribution, weightDistribution, firstParentAsWeight);

    }

    protected void incorporateChildMessage(Map<Category, Map<GradePair, Double>> jointDistributions, Message message) {
        for (Category category : Category.values()) {
            incorporateChildMessageForCategory(category, jointDistributions.get(category), message);
        }
    }

    protected void incorporateChildMessageForCategory(
            Category category,
            Map<GradePair, Double> jointDistribution,
            Message message
    ) {
        Sheep child = (Sheep) message.getSource().getValue();
        Map<Category, PhenotypeAtBirth> phenotypesAtBirth = child.getBirthRecord().getPhenotypesAtBirthOrganized();
        Map<Grade, Double> childDistribution = message.getDistribution().get(category);

        Grade childPhenotype = phenotypesAtBirth.get(category).child();
        Grade phenotype1 = phenotypesAtBirth.get(category).parent1();
        Grade phenotype2 = phenotypesAtBirth.get(category).parent2();

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double jointProb = entry.getValue();

            Map<Grade, Double> expectedChildHidden =
                    InferenceMath.childHiddenDistributionGivenParents(
                            pair,
                            phenotype1,
                            phenotype2,
                            childPhenotype
                    );

            double scalingFactor = 0.0;
            for (Grade grade : Grade.values()) {
                scalingFactor += expectedChildHidden.getOrDefault(grade, 0.0)
                        * childDistribution.getOrDefault(grade, 0.0);
            }

            entry.setValue(jointProb * scalingFactor);
        }

        InferenceMath.normalizeScores(jointDistribution);
    }

    private Map<Grade, Double> halfJointMarginal(Map<GradePair, Double> jointDistribution, Map<Grade, Double> weightDistribution, boolean firstParentAsWeight) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            double probability = entry.getValue();
            Grade weightGrade = firstParentAsWeight ? pair.getFirst() : pair.getSecond();
            Grade targetGrade = firstParentAsWeight ? pair.getSecond() : pair.getFirst();

            newHalfMarginal.merge(targetGrade, probability * weightDistribution.get(weightGrade), Double::sum);
        }
        InferenceMath.normalizeScores(newHalfMarginal);

        return newHalfMarginal;
    }
}
