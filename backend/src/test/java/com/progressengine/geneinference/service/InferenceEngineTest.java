package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import com.progressengine.geneinference.service.AlleleDomains.GradeAlleleDomainTest;
import com.progressengine.geneinference.testutil.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class InferenceEngineTest {
    protected InferenceEngine inferenceEngine;

    @Test
    void testFindJointDistribution() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ));

        Relationship relationship = DomainFixtures.createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.B.code(), 53),
                        Map.entry(Grade.C.code(), 24),
                        Map.entry(Grade.D.code(), 23)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.A.code(), 50),
                        Map.entry(Grade.C.code(), 50)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.S.code(), 25),
                        Map.entry(Grade.A.code(), 25),
                        Map.entry(Grade.C.code(), 26),
                        Map.entry(Grade.D.code(), 24)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S.code(), 53),
                        Map.entry(Grade.C.code(), 47)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.S.code(), 25),
                        Map.entry(Grade.E.code(), 75)
                ))
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
            Map<AllelePair<Grade>, Double> jointDistribution = relationship.getJointDistribution(category);
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
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ));

        Relationship relationship = DomainFixtures.createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.D.code(), 1)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S.code(), 1)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.E.code(), 1)
                ))
        ));

        // Act
        inferenceEngine.findJointDistribution(relationship);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
            Map<AllelePair<Grade>, Double> jointDistribution = relationship.getJointDistribution(category);
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
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ));

        Relationship relationship = DomainFixtures.createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.D.code(), 1)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S.code(), 1)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.E.code(), 1)
                ))
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        inferenceEngine.updateMarginalProbabilities(relationship);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
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
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ));

        Relationship relationship = DomainFixtures.createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.B.code(), 53),
                        Map.entry(Grade.C.code(), 24),
                        Map.entry(Grade.D.code(), 23)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.A.code(), 50),
                        Map.entry(Grade.C.code(), 50)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.S.code(), 25),
                        Map.entry(Grade.A.code(), 25),
                        Map.entry(Grade.C.code(), 26),
                        Map.entry(Grade.D.code(), 24)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S.code(), 53),
                        Map.entry(Grade.C.code(), 47)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.S.code(), 25),
                        Map.entry(Grade.E.code(), 75)
                ))
        ));

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.D.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.S.code()
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
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
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ));

        Relationship relationship = DomainFixtures.createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Category.SWIM, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.FLY, Map.ofEntries(
                        Map.entry(Grade.C.code(), 1)
                )),
                Map.entry(Category.RUN, Map.ofEntries(
                        Map.entry(Grade.D.code(), 1)
                )),
                Map.entry(Category.POWER, Map.ofEntries(
                        Map.entry(Grade.S.code(), 1)
                )),
                Map.entry(Category.STAMINA, Map.ofEntries(
                        Map.entry(Grade.E.code(), 1)
                ))
        ));

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ));

        inferenceEngine.findJointDistribution(relationship);

        // Act
        inferenceEngine.inferChildHiddenDistribution(relationship, child);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
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
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code()
        ), 1);

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code()
        ), 2);

        // Act
        Map<Category, Map<Grade, Double>> prediction = inferenceEngine.predictChildrenDistributions(parent1, parent2);

        // Assert
        for (Category category : Category.values()) {
            if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomainTest)) {
                continue;
            }
            Map<Grade, Double> distribution = prediction.get(category);
            assertNotNull(distribution, "Distribution should not be null");
            assertEquals(6, distribution.size(), "Should have 6 key value pairs for each grade");


            // The sum of probabilities should be ~1.0
            double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
            assertEquals(1.0, sum, 1e-9, "Probabilities should sum to 1.0");
        }
    }
}
