package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.PredictionResponseDTO;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class InferenceEngineTest {
    protected InferenceEngine inferenceEngine;

    @Test
    void testFindJointDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

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
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);
            assertNotNull(jointDistribution, "Joint distribution should not be null");
            assertEquals(36, jointDistribution.size(), "Should have 36 pairs for 6x6 grades");

            // The sum of probabilities should be ~1.0
            double sum = jointDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }

    @Test
    void testFindJointDistributionOneOffspring() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

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
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        for (Category category : Category.values()) {
            Map<GradePair, Double> jointDistribution = relationship.getJointDistribution(category);
            assertNotNull(jointDistribution, "Joint distribution should not be null");
            assertEquals(36, jointDistribution.size(), "Should have 36 pairs for 6x6 grades");

            // The sum of probabilities should be ~1.0
            double sum = jointDistribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }

    @Test
    void testUpdateMarginalDistribution() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

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
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Assert
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

    @Test
    void testInferChildDistributionChild1() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

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
        ));

        Sheep child = createTestSheep(Map.of(
                Category.SWIM, Grade.D,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.S
        ));

        inferenceEngine.findJointDistribution(relationship);

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

    @Test
    void testInferChildDistributionChild2() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

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
        ));

        Sheep child = createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        inferenceEngine.findJointDistribution(relationship);

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

    @Test
    void testPredictChildrenDistributions() {
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

        // Act
        Map<Category, Map<Grade, Double>> prediction = inferenceEngine.predictChildrenDistributions(parent1, parent2).getPhenotypeDistributions();

        // Assert
        for (Category category : Category.values()) {
            Map<Grade, Double> distribution = prediction.get(category);
            assertNotNull(distribution, "Distribution should not be null");
            assertEquals(6, distribution.size(), "Should have 6 key value pairs for each grade");


            // The sum of probabilities should be ~1.0
            double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }

    protected Sheep createTestSheep(Map<Category, Grade> phenotypes) {
        Sheep sheep = new Sheep();
        for (Map.Entry<Category, Grade> entry : phenotypes.entrySet()) {
            sheep.setPhenotype(entry.getKey(), entry.getValue());
        }
        sheep.createDefaultDistributions();
        return sheep;
    }
    protected Sheep createTestSheep(Map<Category, Grade> phenotypes, int sheepId) {
        Sheep sheep = createTestSheep(phenotypes);
        sheep.setId(sheepId);
        return sheep;
    }

    protected Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<Grade, Integer>> offspringPhenotypeFrequency) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        for (Map.Entry<Category, Map<Grade, Integer>> entry : offspringPhenotypeFrequency.entrySet()) {
            relationship.setPhenotypeFrequencies(entry.getKey(), entry.getValue());
        }
        return relationship;
    }

    protected Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Category, Map<Grade, Integer>> offspringPhenotypeFrequency, int relationshipId) {
        Relationship relationship = createTestRelationship(parent1, parent2, offspringPhenotypeFrequency);
        relationship.setId(relationshipId);
        return relationship;
    }
}
