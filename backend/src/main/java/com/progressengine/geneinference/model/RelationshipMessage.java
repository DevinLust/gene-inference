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
        // TODO - experimental cached values
        Map<Category, Map<GradePair, Double>> jointDistributions = relationship.getJointDistributions();
//        for (Category category : Category.values()) {
//            jointDistributions.put(category, relationship.getJointDistribution(category));
//        }

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

    protected void incorporateChildMessage(Map<Category, Map<GradePair, Double>> jointDistributions, Message message) {
        Sheep child = (Sheep) message.getSource().getValue();
        Map<Category, PhenotypeAtBirth> phenotypesAtBirth = child.getBirthRecord().getPhenotypesAtBirthOrganized();

        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = jointDistributions.get(category);
            Map<Grade, Double> childDistribution = message.getDistribution().get(category);

            Grade childPhenotype = phenotypesAtBirth.get(category).child();
            Grade phenotype1 = phenotypesAtBirth.get(category).parent1();
            Grade phenotype2 = phenotypesAtBirth.get(category).parent2();

            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair pair = entry.getKey();
                double jointProb = entry.getValue();

                double[] probFromParents = InferenceMath.probabilityAlleleFromParents(
                        pair,
                        phenotype1,
                        phenotype2,
                        childPhenotype
                );

                double scalingFactor =
                        probFromParents[0] * (childDistribution.getOrDefault(phenotype2, 0.0)
                                + childDistribution.getOrDefault(pair.getSecond(), 0.0))
                                + probFromParents[1] * (childDistribution.getOrDefault(phenotype1, 0.0)
                                + childDistribution.getOrDefault(pair.getFirst(), 0.0));

                entry.setValue(jointProb * scalingFactor);
            }

            InferenceMath.normalizeScores(jointDistribution);
        }
    }

    // TODO - duplicated in LoopyInference
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
