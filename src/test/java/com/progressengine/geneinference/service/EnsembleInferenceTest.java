package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class EnsembleInferenceTest extends InferenceEngineTest {
    @Mock
    protected RelationshipService relationshipService;

    @BeforeEach
    public void setUp() {
        inferenceEngine = new EnsembleInference(relationshipService);
    }

    @Override
    @Test
    void testUpdateMarginalDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ), 1);

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.C, 1)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.C, 1)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.D, 1)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S, 1)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.E, 1)
                ))
        ), 1);

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));

        // Act
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Assert 1
        for (Category category : Category.values()) {
            Map<Grade, Double> hiddenDistribution1 = parent1.getDistribution(category, DistributionType.INFERRED);
            assertNotNull(hiddenDistribution1, "Hidden distribution should not be null");
            assertEquals(6, hiddenDistribution1.size(), "Should have 6 key value pairs for each grade");

            // Assert
            Map<Grade, Double> hiddenDistribution2 = parent2.getDistribution(category, DistributionType.INFERRED);
            assertNotNull(hiddenDistribution2, "Hidden distribution should not be null");
            assertEquals(6, hiddenDistribution2.size(), "Should have 6 key value pairs for each grade");


            // The sum of probabilities should be ~1.0
            double sum1 = hiddenDistribution1.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum1, 1e-9, "Probabilities should sum to 1.0");
            double sum2 = hiddenDistribution2.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum2, 1e-9, "Probabilities should sum to 1.0");
        }
    }

    @Override
    @Test
    void testInferChildDistributionChild1() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ), 1);

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.B, 53),
                        Map.entry(Grade.C, 24),
                        Map.entry(Grade.D, 23)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.A, 50),
                        Map.entry(Grade.C, 50)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.S, 25),
                        Map.entry(Grade.A, 25),
                        Map.entry(Grade.C, 26),
                        Map.entry(Grade.D, 24)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S, 53),
                        Map.entry(Grade.C, 47)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.S, 25),
                        Map.entry(Grade.E, 75)
                ))
        ), 1);

        Sheep child = createTestSheep(Map.of(
                Category.SWIM, Grade.D,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.S
        ));

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);

        // Assert
        for (Category category : Category.values()) {
            Map<Grade, Double> childDistribution = child.getDistribution(category, DistributionType.PRIOR);
            assertNotNull(childDistribution, "Child distribution should not be null");
            assertEquals(6, childDistribution.size(), "Should have 6 key value pairs for each grade");

            double sum = childDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }

    @Override
    @Test
    void testInferChildDistributionChild2() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ), 1);

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ), 2);

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.C, 1)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.C, 1)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.D, 1)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S, 1)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.E, 1)
                ))
        ), 1);

        Sheep child = createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        inferenceEngine.findJointDistribution(relationship);

        when(relationshipService.findRelationshipsByParent(any(Sheep.class))).thenReturn(List.of(relationship));
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);

        // Assert
        for (Category category : Category.values()) {
            Map<Grade, Double> childDistribution = child.getDistribution(category, DistributionType.PRIOR);
            assertNotNull(childDistribution, "Child distribution should not be null");
            assertEquals(6, childDistribution.size(), "Should have 6 key value pairs for each grade");

            double sum = childDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }
}
