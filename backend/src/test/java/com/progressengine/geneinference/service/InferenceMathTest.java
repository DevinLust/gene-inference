package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.AllelePair;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.testutil.ProbabilityAssertions;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InferenceMathTest {
    @Test
    public void testProductOfExpertsCertain() {
        // Arrange
        Map<Grade, Double> existingDistribution = new EnumMap<>(Map.of(
                Grade.S, 0.0,
                Grade.A, 0.5,
                Grade.B, 0.0,
                Grade.C, 0.0,
                Grade.D, 0.0,
                Grade.E, 0.5
        ));
        Map<Grade, Double> newDistribution = Map.of(
                Grade.S, 0.5,
                Grade.A, 0.0,
                Grade.B, 0.0,
                Grade.C, 0.0,
                Grade.D, 0.0,
                Grade.E, 0.5
        );

        // Act
        InferenceMath.productOfExperts(existingDistribution, newDistribution);

        // Assert
        assertEquals(1.0, existingDistribution.get(Grade.E), 1e-9, "Probability for grade E should be 1.0");

        ProbabilityAssertions.assertValidDistribution(existingDistribution);
    }

    @Test
    public void testProductOfExpertsTwoUniform() {
        // Arrange
        Map<Grade, Double> existingDistribution = SheepService.createUniformDistribution();
        Map<Grade, Double> newDistribution = SheepService.createUniformDistribution();

        // Act
        InferenceMath.productOfExperts(existingDistribution, newDistribution);

        // Assert
        ProbabilityAssertions.assertUniform(existingDistribution, 1e-9);

        ProbabilityAssertions.assertValidDistribution(existingDistribution);
    }

    @Test
    public void testProductOfExpertsOneUniform() {
        // Arrange
        Map<Grade, Double> existingDistribution = SheepService.createUniformDistribution();
        Map<Grade, Double> newDistribution = Map.of(
                Grade.S, 0.5,
                Grade.A, 0.0,
                Grade.B, 0.0,
                Grade.C, 0.0,
                Grade.D, 0.0,
                Grade.E, 0.5
        );

        // Act
        InferenceMath.productOfExperts(existingDistribution, newDistribution);

        // Assert
        for (Map.Entry<Grade, Double> entry : existingDistribution.entrySet()) {
            assertEquals(newDistribution.get(entry.getKey()), entry.getValue(), 1e-9, "Probability for grade " + entry.getKey() + " should be " + newDistribution.get(entry.getKey()));
        }

        ProbabilityAssertions.assertValidDistribution(existingDistribution);
    }

    @Test
    public void testProbabilityAlleleFromParentsAllUniqueAlleles() {
        // Arrange
        Grade phenotypeParent1 = Grade.S;
        Grade phenotypeParent2 = Grade.A;
        GradePair hiddenPair = new GradePair(Grade.B, Grade.C);
        Grade childPhenotype = Grade.B;

        // Act
        double[] probFromParents = InferenceMath.probabilityAlleleFromParents(hiddenPair, phenotypeParent1, phenotypeParent2, childPhenotype);

        // Assert
        assertEquals(1.0, probFromParents[0], 1e-9, "Probability phenotype comes from parent 1 should be 1.0");
        assertEquals(0.0, probFromParents[1],  1e-9, "Probability phenotype comes from parent 2 should be 0.0");
    }

    @Test
    public void testProbabilityAlleleFromParentsCommonAllele() {
        // Arrange
        Grade phenotypeParent1 = Grade.S;
        Grade phenotypeParent2 = Grade.A;
        GradePair hiddenPair = new GradePair(Grade.B, Grade.S);
        Grade childPhenotype = Grade.S;

        // Act
        double[] probFromParents = InferenceMath.probabilityAlleleFromParents(hiddenPair, phenotypeParent1, phenotypeParent2, childPhenotype);

        // Assert
        assertEquals(0.5, probFromParents[0], 1e-9, "Probability phenotype comes from parent 1 should be 0.5");
        assertEquals(0.5, probFromParents[1],  1e-9, "Probability phenotype comes from parent 2 should be 0.5");
    }

    @Test
    public void testProbabilityAlleleFromParentsTwoToOne() {
        // Arrange
        Grade phenotypeParent1 = Grade.S;
        Grade phenotypeParent2 = Grade.A;
        GradePair hiddenPair = new GradePair(Grade.A, Grade.A);
        Grade childPhenotype = Grade.A;

        // Act
        double[] probFromParents = InferenceMath.probabilityAlleleFromParents(hiddenPair, phenotypeParent1, phenotypeParent2, childPhenotype);

        // Assert
        assertEquals(5.0 / 13.0, probFromParents[0], 1e-9, "Probability phenotype comes from parent 1");
        assertEquals(8.0 / 13.0, probFromParents[1],  1e-9, "Probability phenotype comes from parent 2");
    }

    @Test
    public void testProbabilityAlleleFromParentsTwoMiddleRanked() {
        // Arrange
        Grade phenotypeParent1 = Grade.B;
        Grade phenotypeParent2 = Grade.B;
        GradePair hiddenPair = new GradePair(Grade.D, Grade.A);
        Grade childPhenotype = Grade.B;

        // Act
        double[] probFromParents = InferenceMath.probabilityAlleleFromParents(hiddenPair, phenotypeParent1, phenotypeParent2, childPhenotype);

        // Assert
        assertEquals(0.4, probFromParents[0], 1e-9, "Probability phenotype comes from parent 1");
        assertEquals(0.6, probFromParents[1],  1e-9, "Probability phenotype comes from parent 2");
    }

    @Test
    public void testNormalizeScoresGradeDistribution() {
        // Arrange
        Map<Grade, Double> distribution = new EnumMap<>(Map.of(
                Grade.S, 1.0,
                Grade.A, 2.5,
                Grade.B, 0.332,
                Grade.C, 10.0,
                Grade.D, 0.3,
                Grade.E, 0.5
        ));

        // Act
        InferenceMath.normalizeScores(distribution);

        // Assert
        ProbabilityAssertions.assertValidDistribution(distribution);
        assertEquals(distribution.get(Grade.C), 10 * distribution.get(Grade.S), "Probability of Grade C should be 10x higher than Grade S");
    }

    @Test
    public void testMultinomialScoresGradeDistribution() {
        // Arrange
        Map<Grade, Integer> phenotypeFrequencies = new EnumMap<>(Map.of(
                Grade.S, 4,
                Grade.B, 4,
                Grade.C, 2,
                Grade.E, 1
                ));
        Grade parent1Phenotype = Grade.S;
        Grade parent2Phenotype = Grade.C;
        AlleleDomain<Grade> domain = gradeDomain();

        // Act
        Map<AllelePair<Grade>, Double> jointDist = InferenceMath.multinomialJointScores(parent1Phenotype, parent2Phenotype, phenotypeFrequencies, domain);

        // Assert
        ProbabilityAssertions.assertValidDistribution(jointDist);
        assertEquals(0.5803566632647579, jointDist.get(new AllelePair<>(Grade.B, Grade.E)), 1e-6);
        assertEquals(0.4196433367352421, jointDist.get(new AllelePair<>(Grade.E, Grade.B)), 1e-6);
    }

    @Test
    void multinomialScore_shouldGiveSameScoreFor_BE_and_EB_whenPhenotypesAreSAndB() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Integer> phenotypeFrequency = new EnumMap<>(Grade.class);
        phenotypeFrequency.put(Grade.S, 5);
        phenotypeFrequency.put(Grade.B, 8);
        phenotypeFrequency.put(Grade.E, 2);

        double scoreBE = InferenceMath.multinomialScore(
                new AllelePair<>(Grade.B, Grade.E),
                Grade.S,
                Grade.B,
                phenotypeFrequency,
                domain
        );

        double scoreEB = InferenceMath.multinomialScore(
                new AllelePair<>(Grade.E, Grade.B),
                Grade.S,
                Grade.B,
                phenotypeFrequency,
                domain
        );

        double expected =
                1_000_000 * // scaling constant from the method
                        Math.pow(0.35, 5) * // P(S)
                        Math.pow(0.50, 8) * // P(B)
                        Math.pow(0.15, 2);  // P(E)

        assertEquals(expected, scoreBE, 1e-12, "score for BE is wrong");
        assertEquals(expected, scoreEB, 1e-12, "score for EB is wrong");
    }

    @Test
    void childHiddenDistribution_givenPhenotypeS_returnsA50E50() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.B, Grade.E),
                Grade.S,
                Grade.A,
                Grade.S,
                domain
        );

        assertEquals(0.5, dist.get(Grade.A), 1e-12);
        assertEquals(0.5, dist.get(Grade.E), 1e-12);
        assertEquals(0.0, dist.get(Grade.S), 1e-12);
        assertEquals(0.0, dist.get(Grade.B), 1e-12);
        assertEquals(0.0, dist.get(Grade.C), 1e-12);
        assertEquals(0.0, dist.get(Grade.D), 1e-12);

        assertEquals(1.0, dist.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void childHiddenDistribution_givenPhenotypeB_forSB_BE_returnsExpectedDistribution() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.B, Grade.E),
                Grade.S,
                Grade.B,
                Grade.B,
                domain
        );

        assertEquals(0.15, dist.get(Grade.S), 1e-12);
        assertEquals(0.50, dist.get(Grade.B), 1e-12);
        assertEquals(0.35, dist.get(Grade.E), 1e-12);
        assertEquals(0.0, dist.get(Grade.A), 1e-12);
        assertEquals(0.0, dist.get(Grade.C), 1e-12);
        assertEquals(0.0, dist.get(Grade.D), 1e-12);

        assertEquals(1.0, dist.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void childHiddenDistribution_givenPhenotypeB_forSB_EB_returnsExpectedDistribution() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.E, Grade.B),
                Grade.S,
                Grade.B,
                Grade.B,
                domain
        );

        assertEquals(0.30, dist.get(Grade.S), 1e-12);
        assertEquals(0.70, dist.get(Grade.E), 1e-12);
        assertEquals(0.0, dist.get(Grade.B), 1e-12);
        assertEquals(0.0, dist.get(Grade.A), 1e-12);
        assertEquals(0.0, dist.get(Grade.C), 1e-12);
        assertEquals(0.0, dist.get(Grade.D), 1e-12);

        assertEquals(1.0, dist.values().stream().mapToDouble(Double::doubleValue).sum(), 1e-12);
    }

    @Test
    void childHiddenDistribution_givenPhenotypeE_forSB_BE_returnsS50B50() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.B, Grade.E),
                Grade.S,
                Grade.B,
                Grade.E,
                domain
        );

        assertEquals(0.5, dist.get(Grade.S), 1e-12);
        assertEquals(0.5, dist.get(Grade.B), 1e-12);
        assertEquals(0.0, dist.get(Grade.E), 1e-12);
        assertEquals(0.0, dist.get(Grade.A), 1e-12);
        assertEquals(0.0, dist.get(Grade.C), 1e-12);
        assertEquals(0.0, dist.get(Grade.D), 1e-12);
    }

    @Test
    void childHiddenDistribution_givenImpossiblePhenotype_returnsAllZero() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.B, Grade.E),
                Grade.S,
                Grade.A,
                Grade.D,
                domain
        );

        for (Grade grade : Grade.values()) {
            assertEquals(0.0, dist.get(grade), 1e-12);
        }
    }

    @Test
    void childHiddenDistributionGivenParents_homozygousHiddenPair_returnsExpectedDistribution() {
        AlleleDomain<Grade> domain = gradeDomain();
        Map<Grade, Double> dist = InferenceMath.childHiddenDistributionGivenParents(
                new AllelePair<>(Grade.C, Grade.C),
                Grade.B,
                Grade.A,
                Grade.C,
                domain
        );

        assertEquals(3.0 / 16.0, dist.get(Grade.A), 1e-12);
        assertEquals(3.0 / 16.0, dist.get(Grade.B), 1e-12);
        assertEquals(10.0 / 16.0, dist.get(Grade.C), 1e-12);
        assertEquals(0.0, dist.get(Grade.S), 1e-12);
        assertEquals(0.0, dist.get(Grade.D), 1e-12);
        assertEquals(0.0, dist.get(Grade.E), 1e-12);

        ProbabilityAssertions.assertValidDistribution(dist);
    }

    private AlleleDomain<Grade> gradeDomain() {
        return CategoryDomains.typedDomainFor(Category.SWIM);
    }
}
