package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
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
        Map<Category, Map<GradePair, Double>> jointDistributions = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            jointDistributions.put(category, relationship.getJointDistribution(category));
        }

        // need to incorporate child messages into the joint before marginalization
        for (int i = 1; i < messages.size(); i++) {
            Message message = messages.get(i);
            for (Category category : Category.values()) {
                incorporateChildMessage(jointDistributions.get(category), message, relationship.getParent1().getPhenotype(category), relationship.getParent2().getPhenotype(category), category);
            }
        }

        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, halfJointMarginal(jointDistributions, weightDistribution.get(category), firstParentAsWeight, category));
        }
        return result;
    }

    private void incorporateChildMessage(Map<GradePair, Double> jointDistribution, Message message, Grade phenotype1, Grade phenotype2, Category category) {
        Grade childPhenotype = ((Sheep) message.getSource().getValue()).getPhenotype(category);
        Map<Grade, Double> childDistribution = message.getDistribution().get(category);

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair pair = entry.getKey();
            Double jointProb = entry.getValue();
            double[] probFromParents = probabilityAlleleFromParents(pair, phenotype1, phenotype2, childPhenotype);
            // probability phenotype came from a parent multiplied the probability the child can have each of the other alleles from the other parent
            double scalingFactor = probFromParents[0] * (childDistribution.get(phenotype2) + childDistribution.get(pair.getSecond())) +
                    probFromParents[1] * (childDistribution.get(phenotype1) + childDistribution.get(pair.getFirst()));
            entry.setValue(scalingFactor * jointProb);
        }

        normalizeScores(jointDistribution);
    }

    private Map<Grade, Double> halfJointMarginal(Map<Category, Map<GradePair, Double>> jointDistributions, Map<Grade, Double> weightDistribution, boolean firstParentAsWeight, Category category) {
        Map<Grade, Double> newHalfMarginal = new EnumMap<>(Grade.class);
        Map<GradePair, Double> jointDistribution = jointDistributions.get(category);

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
