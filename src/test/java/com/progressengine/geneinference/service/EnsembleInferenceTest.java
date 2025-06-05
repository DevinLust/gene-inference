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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class EnsembleInferenceTest extends InferenceEngineTest {
    @Mock
    private RelationshipService relationshipService;

    @BeforeEach
    public void setUp() {
        inferenceEngine = new EnsembleInference(relationshipService);
    }

    @Override
    @Test
    void testUpdateMarginalDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ));

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));

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
}
