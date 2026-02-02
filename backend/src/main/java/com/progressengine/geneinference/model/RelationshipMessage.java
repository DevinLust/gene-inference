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
        Map<Category, Map<GradePair, Double>> jointDistributions = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            jointDistributions.put(category, relationship.getJointDistribution(category));
        }

        // need to incorporate child messages into the joint before marginalization
        for (int i = 1; i < messages.size(); i++) {
            incorporateChildMessage(jointDistributions, messages.get(i), relationship.getParent1(), relationship.getParent2());
        }

        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            result.put(category, halfJointMarginal(jointDistributions.get(category), weightDistribution.get(category), firstParentAsWeight));
        }
        return result;
    }

    protected void incorporateChildMessage(Map<Category, Map<GradePair, Double>> jointDistributions, Message message, Sheep parent1, Sheep parent2) {
        Sheep child = (Sheep) message.getSource().getValue();
        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = jointDistributions.get(category);

            Map<Grade, Double> childDistribution = message.getDistribution().get(category);

            Grade childPhenotype = child.getPhenotype(category);
            Grade phenotype1 = parent1.getPhenotype(category);
            Grade phenotype2 = parent2.getPhenotype(category);

            // each hidden pair is scaled by how likely the phenotype is to come from one and how
            // likely the other contributed the hidden allele
            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair pair = entry.getKey();
                Double jointProb = entry.getValue();
                double[] probFromParents = InferenceMath.probabilityAlleleFromParents(pair, phenotype1, phenotype2, childPhenotype);
                // probability phenotype came from a parent multiplied the probability the child can have each of the other alleles from the other parent
                double scalingFactor = probFromParents[0] * (childDistribution.get(phenotype2) + childDistribution.get(pair.getSecond())) +
                        probFromParents[1] * (childDistribution.get(phenotype1) + childDistribution.get(pair.getFirst()));
                entry.setValue(scalingFactor * jointProb);
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
