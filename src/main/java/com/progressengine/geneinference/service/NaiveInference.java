package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.HashMap;
import java.util.Map;

public class NaiveInference implements InferenceEngine {

    public void findJointDistribution(Relationship relationship) {
        Map<GradePair, Double> intermediateScores = new HashMap<>();
        Map<Grade, Integer> phenotypeFrequency = relationship.getOffspringPhenotypeFrequency();
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        Grade phenotype1 = parent1.getPhenotype();
        Grade phenotype2 = parent2.getPhenotype();

        for (Grade grade1 : Grade.values()) {
            for (Grade grade2 : Grade.values()) {
                GradePair gradePair = new GradePair(grade1, grade2);
                double multiScore = multinomialScore(gradePair, phenotype1, phenotype2, phenotypeFrequency);
                intermediateScores.put(gradePair, multiScore * parent1.getHiddenDistribution().get(grade1) * parent2.getHiddenDistribution().get(grade2));
            }
        }

        normalizeScores(intermediateScores);
        relationship.setHiddenPairsDistribution(intermediateScores);
    }

    public Map<Grade, Double> inferChildHiddenDistribution(Sheep parent1, Sheep parent2) {
        Map<Grade, Double> childHiddenDistribution = new HashMap<>();

        for (Grade grade : Grade.values()) {
            childHiddenDistribution.putIfAbsent(grade, 1.0 / 6.0);
        }

        return childHiddenDistribution;
    }

    private double multinomialScore(GradePair hiddenPair, Grade phenotype1, Grade phenotype2, Map<Grade, Integer> phenotypeFrequency) {
        double score = 1.0;

        // each occurrence of a grade adds 1/4 to the probability of that grade
        Map<Grade, Double> probabilityToDraw = new HashMap<>();
        probabilityToDraw.merge(phenotype1, 0.25, Double::sum);
        probabilityToDraw.merge(phenotype2, 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getFirst(), 0.25, Double::sum);
        probabilityToDraw.merge(hiddenPair.getSecond(), 0.25, Double::sum);

        for (Grade grade : Grade.values()) {
            Double probability = probabilityToDraw.getOrDefault(grade, 0.0);
            Integer frequency = phenotypeFrequency.get(grade);
            if (probability == 0.0 && frequency > 0) {
                score = 0.0;
            } else if (frequency > 0) {
                score *= Math.exp(frequency * Math.log(probability));
            }
        }

        return score;
    }

    // normalize the given Map of scores regardless of the key type
    private <T> void normalizeScores(Map<T, Double> scores) {
        double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) { return; }

        for (Map.Entry<T, Double> entry : scores.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }
    }

}
