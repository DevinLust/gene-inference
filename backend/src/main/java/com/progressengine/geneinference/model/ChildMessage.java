package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.InferenceMath;
import com.progressengine.geneinference.service.SheepService;

import java.util.EnumMap;
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
        Sheep child = target.getValue();
        Message parent1Message = messages.get(0);
        Message parent2Message = messages.get(1);

        Map<Category, Map<GradePair, Double>> jointDistributions = relationship.getJointDistributions();

        // incorporate the parent messages into the joint distribution
        incorporateParents(jointDistributions, parent1Message, parent2Message);

        // incorporate the sibling messages into the joint distribution
        for (Message siblingMessage : messages.subList(2, messages.size())) {
            incorporateChildMessage(jointDistributions, siblingMessage);
        }

        // add the conditional distributions of seeing the child phenotype
        return accumulateConditionals(jointDistributions, child);
    }

    private void incorporateParents(Map<Category, Map<GradePair, Double>> jointDistributions, Message parent1Message, Message parent2Message) {
        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = jointDistributions.get(category);
            Map<Grade, Double> parent1Dist = parent1Message.getDistribution().get(category);
            Map<Grade, Double> parent2Dist = parent2Message.getDistribution().get(category);
            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair pair = entry.getKey();
                Double oldProb = entry.getValue();
                entry.setValue(oldProb * parent1Dist.get(pair.getFirst()) *  parent2Dist.get(pair.getSecond()));
            }
            InferenceMath.normalizeScores(jointDistribution);
        }
    }

    // TODO - nearly identical algorithm in EnsembleInference
    // Iterates through possible hidden pairs and weighting and accumulating their distributions
    private Map<Category, Map<Grade, Double>> accumulateConditionals(Map<Category, Map<GradePair, Double>> jointDistributions, Sheep child) {
        Map<Category, Map<Grade, Double>> result = new EnumMap<>(Category.class);
        Map<Category, PhenotypeAtBirth> phenotypesAtBirth = child.getBirthRecord().getPhenotypesAtBirthOrganized();

        for (Category category : Category.values()) {
            Map<Grade, Double> catResult = new EnumMap<>(Grade.class);
            fillMissingWithZero(catResult);
            Map<GradePair, Double> jointDistribution = jointDistributions.get(category);
            Grade parent1Phenotype = phenotypesAtBirth.get(category).parent1();
            Grade parent2Phenotype = phenotypesAtBirth.get(category).parent2();
            Grade childPhenotype = phenotypesAtBirth.get(category).child();

            for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
                GradePair pair = entry.getKey();
                Map<Grade, Double> conditionalDist =
                        InferenceMath.childHiddenDistributionGivenParents(
                                pair,
                                parent1Phenotype,
                                parent2Phenotype,
                                childPhenotype
                        );
                addDistributions(catResult, conditionalDist, jointDistribution.get(pair));
            }

            InferenceMath.normalizeScores(catResult);
            result.put(category, catResult);
        }

        return result;
    }

    // returns the expected distribution of the hidden allele of the child assuming these alleles for the parents
    private Map<Grade, Double> combineConditional(double[] probFromParents, GradePair pair, Grade phenotype1, Grade phenotype2) {
        Map<Grade, Double> result = new EnumMap<>(Grade.class);
        for (Grade grade : Grade.values()) {
            result.put(grade, 0.0);
        }
        // prob child's phen came from other parent * chance this parent's allele was picked
        result.merge(phenotype1, probFromParents[1] * 0.5, Double::sum);
        result.merge(pair.getFirst(), probFromParents[1] * 0.5, Double::sum);
        result.merge(phenotype2, probFromParents[0] * 0.5, Double::sum);
        result.merge(pair.getSecond(), probFromParents[0] * 0.5, Double::sum);

        InferenceMath.normalizeScores(result);
        return result;
    }

    private void addDistributions(Map<Grade, Double> distribution, Map<Grade, Double> weightedDist, double weight) {
        for (Map.Entry<Grade, Double> entry : distribution.entrySet()) {
            entry.setValue(entry.getValue() + weight * weightedDist.get(entry.getKey()));
        }
    }

    private void fillMissingWithZero(Map<Grade, Double> distribution) {
        for (Grade grade : Grade.values()) {
            distribution.putIfAbsent(grade, 0.0);
        }
    }
}
