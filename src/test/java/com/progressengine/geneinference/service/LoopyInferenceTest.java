package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoopyInferenceTest extends EnsembleInferenceTest {
    @BeforeEach
    public void setUp() {
        inferenceEngine = new LoopyInference(relationshipService);
    }

    @Override
    @Test
    void testUpdateMarginalDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution(), 1);

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution(), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ), 1);

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));

        // Act
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Assert 1
        Map<Grade, Double> hiddenDistribution1 = parent1.getHiddenDistribution();
        assertNotNull(hiddenDistribution1, "Hidden distribution should not be null");
        assertEquals(6, hiddenDistribution1.size(), "Should have 6 key value pairs for each grade");

        for (Map.Entry<Grade, Double> entry : hiddenDistribution1.entrySet()) {
            Grade key = entry.getKey();
            Double probability = entry.getValue();
            if (key.equals(Grade.C)) {
                assertTrue(probability > 0.5, "Probability should be greater than 0.5");
            } else {
                assertTrue(probability <= 0.5, "Probability should be less than 0.5");
            }
        }

        // Assert 2
        Map<Grade, Double> hiddenDistribution2 = parent2.getHiddenDistribution();
        assertNotNull(hiddenDistribution2, "Hidden distribution should not be null");
        assertEquals(6, hiddenDistribution2.size(), "Should have 6 key value pairs for each grade");

        for (Map.Entry<Grade, Double> entry : hiddenDistribution2.entrySet()) {
            Grade key = entry.getKey();
            Double probability = entry.getValue();
            if (key.equals(Grade.C)) {
                assertTrue(probability > 0.5, "Probability should be greater than 0.5");
            } else {
                assertTrue(probability <= 0.5, "Probability should be less than 0.5");
            }
        }

        // The sum of probabilities should be ~1.0
        double sum1 = hiddenDistribution1.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum1, 1e-9, "Probabilities should sum to 1.0");
        double sum2 = hiddenDistribution2.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum2, 1e-9, "Probabilities should sum to 1.0");
    }

    @Override
    @Test
    void testInferChildDistributionChild1() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.B, SheepService.createUniformDistribution(), 1);

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution(), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.B, 53),
                Map.entry(Grade.C, 24),
                Map.entry(Grade.D, 23)
        ), 1);

        Sheep child = createTestSheep(Grade.D, SheepService.createUniformDistribution());

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);
        Map<Grade, Double> childDistribution = child.getPriorDistribution();

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

    @Override
    @Test
    void testInferChildDistributionChild2() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution(), 1);

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution(), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ), 1);

        Sheep child = createTestSheep(Grade.C, SheepService.createUniformDistribution());

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);
        Map<Grade, Double> childDistribution = child.getPriorDistribution();

        // Assert
        assertNotNull(childDistribution, "Child distribution should not be null");
        assertEquals(6, childDistribution.size(), "Should have 6 key value pairs for each grade");
    }
}
