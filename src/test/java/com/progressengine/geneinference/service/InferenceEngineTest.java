package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class InferenceEngineTest {
    protected InferenceEngine inferenceEngine;

    @Test
    void testFindJointDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.B, 53),
                Map.entry(Grade.C, 24),
                Map.entry(Grade.D, 23)
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();
        assertNotNull(jointDistribution, "Joint distribution should not be null");
        assertEquals(36, jointDistribution.size(), "Should have 36 pairs for 6x6 grades");

        GradePair firstKeyPair = new GradePair(Grade.C, Grade.D);
        assertEquals(0.5, jointDistribution.get(firstKeyPair), 0.01);

        GradePair secondKeyPair = new GradePair(Grade.D, Grade.C);
        assertEquals(0.5, jointDistribution.get(secondKeyPair), 0.01);

        // The sum of probabilities should be ~1.0
        double sum = jointDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
    }

    @Test
    void testFindJointDistributionOneOffspring() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        Map<GradePair, Double> jointDistribution = relationship.getHiddenPairsDistribution();
        assertNotNull(jointDistribution, "Joint distribution should not be null");
        assertEquals(36, jointDistribution.size(), "Should have 36 pairs for 6x6 grades");

        for (Map.Entry<GradePair, Double> entry : jointDistribution.entrySet()) {
            GradePair key = entry.getKey();
            Double probability = entry.getValue();
            if (key.getFirst().equals(Grade.C) && key.getSecond().equals(Grade.C)) {
                assertEquals(2.0 / 12.0, probability, 0.01);
            } else if (key.getFirst().equals(Grade.C) || key.getSecond().equals(Grade.C)) {
                assertEquals(1.0 / 12.0, probability, 0.01);
            } else {
                assertEquals(0.0, probability, 0.01);
            }
        }

        // The sum of probabilities should be ~1.0
        double sum = jointDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
    }

    @Test
    void testUpdateMarginalDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Assert
        Map<Grade, Double> hiddenDistribution1 = parent1.getHiddenDistribution();
        assertNotNull(hiddenDistribution1, "Hidden distribution should not be null");
        assertEquals(6, hiddenDistribution1.size(), "Should have 6 key value pairs for each grade");

        for (Map.Entry<Grade, Double> entry : hiddenDistribution1.entrySet()) {
            Grade key = entry.getKey();
            Double probability = entry.getValue();
            if (key.equals(Grade.C)) {
                assertEquals(7.0 / 12.0, probability, 0.01);
            } else {
                assertEquals(1.0 / 12.0, probability, 0.01);
            }
        }

        // Assert
        Map<Grade, Double> hiddenDistribution2 = parent2.getHiddenDistribution();
        assertNotNull(hiddenDistribution2, "Hidden distribution should not be null");
        assertEquals(6, hiddenDistribution2.size(), "Should have 6 key value pairs for each grade");

        for (Map.Entry<Grade, Double> entry : hiddenDistribution2.entrySet()) {
            Grade key = entry.getKey();
            Double probability = entry.getValue();
            if (key.equals(Grade.C)) {
                assertEquals(7.0 / 12.0, probability, 0.01);
            } else {
                assertEquals(1.0 / 12.0, probability, 0.01);
            }
        }

        // The sum of probabilities should be ~1.0
        double sum1 = hiddenDistribution1.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum1, 1e-9, "Probabilities should sum to 1.0");
        double sum2 = hiddenDistribution2.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum2, 1e-9, "Probabilities should sum to 1.0");
    }

    @Test
    void testInferChildDistributionChild1() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.B, 53),
                Map.entry(Grade.C, 24),
                Map.entry(Grade.D, 23)
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        Map<Grade, Double> childDistribution = inferenceEngine.inferChildHiddenDistribution(relationship, Grade.D);

        // Assert
        assertNotNull(childDistribution, "Child distribution should not be null");
        assertEquals(6, childDistribution.size(), "Should have 6 key value pairs for each grade");

        for (Map.Entry<Grade, Double> entry : childDistribution.entrySet()) {
            Grade key = entry.getKey();
            Double probability = entry.getValue();

            if (key.equals(Grade.B) || key.equals(Grade.C)) {
                assertEquals(0.5, probability, 0.01);
            } else {
                assertEquals(0.0, probability, 0.01);
            }
        }
    }

    @Test
    void testInferChildDistributionChild2() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        Map<Grade, Double> childDistribution = inferenceEngine.inferChildHiddenDistribution(relationship, Grade.C);

        // Assert
        assertNotNull(childDistribution, "Child distribution should not be null");
        assertEquals(6, childDistribution.size(), "Should have 6 key value pairs for each grade");
    }

    protected Sheep createTestSheep(Grade phenotype, Map<Grade, Double> hiddenDistribution) {
        Sheep sheep = new Sheep();
        sheep.setPhenotype(phenotype);
        sheep.setHiddenDistribution(hiddenDistribution);
        return sheep;
    }

    protected Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Grade, Integer> offspringPhenotypeFrequency) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        relationship.setOffspringPhenotypeFrequency(offspringPhenotypeFrequency);
        return relationship;
    }
}
