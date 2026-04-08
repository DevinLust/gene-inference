package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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

        // Act
        Sheep[] sheepChildren = new Sheep[10];
        for (int i = 0; i < sheepChildren.length; i++) {
            Sheep newChild = BreedingService.breedNewSheep(parent1, parent2);
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

    private Sheep createTestSheep(Map<Category, SheepGenotypeDTO> genotypes) {
        Sheep sheep = new Sheep();
        sheep.setGenotypes(genotypes);
        sheep.createDefaultDistributions();
        return sheep;
    }

    private Grade randomGrade() {
        return Grade.values()[random.nextInt(Grade.values().length)];
    }
}
