package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BreedingServiceTest {

    private static final Random random = new Random(42);

    @Test
    void testBreedNewSheep() {
        // Arrange
        Grade parent1Phenotype = Grade.A;
        Grade parent2Phenotype = Grade.C;
        Grade parent1HiddenAllele = Grade.S;
        Grade parent2HiddenAllele = Grade.B;
        Set<Grade> parent1Genotype = EnumSet.of(parent1Phenotype, parent1HiddenAllele);
        Set<Grade> parent2Genotype = EnumSet.of(parent2Phenotype, parent2HiddenAllele);
        Set<Grade> allAlleles = EnumSet.of(parent1Phenotype, parent1HiddenAllele, parent2Phenotype, parent2HiddenAllele);

        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(parent1Phenotype, parent1HiddenAllele),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(parent2Phenotype, parent2HiddenAllele),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));

        Relationship relationship = createTestRelationship(parent1, parent2);

        // Act
        Sheep[] sheepChildren = new Sheep[10];
        for (int i = 0; i < sheepChildren.length; i++) {
            Sheep newChild = BreedingService.breedNewSheep(relationship);
            sheepChildren[i] = newChild;
        }

        // Assert
        for (Sheep child : sheepChildren) {
            assertNotNull(child, "child should not be null");
            assertTrue(allAlleles.contains(child.getPhenotype(Category.SWIM)), "phenotype should be in the set of possible alleles");
            assertTrue(allAlleles.contains(child.getHiddenAllele(Category.SWIM)), "hidden allele should be in the set of possible alleles");
            if (parent1Genotype.contains(child.getPhenotype(Category.SWIM))) {
                assertTrue(parent2Genotype.contains(child.getHiddenAllele(Category.SWIM)), "hidden allele should be from parent 2");
            } else if (parent2Genotype.contains(child.getPhenotype(Category.SWIM))) {
                assertTrue(parent1Genotype.contains(child.getHiddenAllele(Category.SWIM)), "hidden allele should be from parent 1");
            } else {
                fail("impossible phenotype: " + child.getPhenotype(Category.SWIM));
            }
        }
    }

    @Test
    void testBreedNewSheepSimpleDistribution() {
        // Arrange
        Grade parent1Phenotype = Grade.A;
        Grade parent2Phenotype = Grade.B;
        Grade parent1HiddenAllele = Grade.C;
        Grade parent2HiddenAllele = Grade.D;
        Set<Grade> allAlleles = EnumSet.of(parent1Phenotype, parent1HiddenAllele, parent2Phenotype, parent2HiddenAllele);

        int N = 100; // Number of trials
        double p = 0.25; // Each outcome's probability
        double expectedCount = N * p;
        double sigma = Math.sqrt(N * p * (1 - p));
        double lowerBound = expectedCount - 3 * sigma;
        double upperBound = expectedCount + 3 * sigma;

        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(parent1Phenotype, parent1HiddenAllele),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));

        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(parent2Phenotype, parent2HiddenAllele),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));

        Relationship relationship = createTestRelationship(parent1, parent2);

        // Act
        for (int i = 0; i < N; i++) {
            Sheep child = BreedingService.breedNewSheep(relationship);
            relationship.addChildInformationToRelationship(child);
        }


        // Assert for distribution
        Map<Grade, Integer> counts = relationship.getCurrentPhenotypeFrequencies(Category.SWIM);
        for (Map.Entry<Grade, Integer> entry : counts.entrySet()) {
            Grade grade = entry.getKey();
            int count = entry.getValue();
            if (allAlleles.contains(grade)) {
                assertTrue(count >= lowerBound && count <= upperBound,
                        "Outcome " + entry.getKey() + " count " + count + " is outside range [" + lowerBound + ", " + upperBound + "]");
            } else {
                assertEquals(0, count, "Grade " + grade + " should not be possible");
            }
        }
    }

    private Sheep createTestSheep(Map<Category, SheepGenotypeDTO> genotypes) {
        Sheep sheep = new Sheep();
        sheep.setGenotypes(genotypes);
        sheep.createDefaultDistributions();
        return sheep;
    }

    private Relationship createTestRelationship(Sheep parent1, Sheep parent2) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        return relationship;
    }

    private Grade randomGrade() {
        return Grade.values()[random.nextInt(Grade.values().length)];
    }
}
