package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.*;
import com.progressengine.geneinference.testutil.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BreedingServiceTest {

    private static final Random random = new Random(42);

    @Test
    void testBreedNewSheepSwim() {
        testBreedNewSheepForCategory(
                Category.SWIM,
                Grade.A,
                Grade.S,
                Grade.C,
                Grade.B
        );
    }

    @Test
    void testBreedNewSheepTone() {
        testBreedNewSheepForCategory(
                Category.TONE,
                Tone.TWO_TONE,
                Tone.TWO_TONE,
                Tone.TWO_TONE,
                Tone.MONOTONE
        );
    }

    @Test
    void testBreedNewSheepColor() {
        testBreedNewSheepForCategory(
                Category.COLOR,
                Color.NORMAL,
                Color.NORMAL,
                Color.RED,
                Color.NORMAL
        );
    }

    private <A extends Enum<A> & Allele> void testBreedNewSheepForCategory(
            Category categoryUnderTest,
            A parent1Phenotype,
            A parent1HiddenAllele,
            A parent2Phenotype,
            A parent2HiddenAllele
    ) {
        Set<A> parent1Genotype = EnumSet.of(parent1Phenotype, parent1HiddenAllele);
        Set<A> parent2Genotype = EnumSet.of(parent2Phenotype, parent2HiddenAllele);
        Set<A> allAlleles = EnumSet.of(
                parent1Phenotype,
                parent1HiddenAllele,
                parent2Phenotype,
                parent2HiddenAllele
        );

        Sheep parent1 = DomainFixtures.createTestSheepWithFullGenotype(Map.of(
                categoryUnderTest,
                new SheepGenotypeDTO(parent1Phenotype.code(), parent1HiddenAllele.code())
        ));

        Sheep parent2 = DomainFixtures.createTestSheepWithFullGenotype(Map.of(
                categoryUnderTest,
                new SheepGenotypeDTO(parent2Phenotype.code(), parent2HiddenAllele.code())
        ));

        Sheep[] sheepChildren = new Sheep[10];
        for (int i = 0; i < sheepChildren.length; i++) {
            sheepChildren[i] = BreedingService.breedNewSheep(parent1, parent2);
        }

        for (Sheep child : sheepChildren) {
            A phenotype = child.getPhenotype(categoryUnderTest);
            A hiddenAllele = child.getHiddenAllele(categoryUnderTest);

            assertNotNull(child, "child should not be null");
            assertTrue(allAlleles.contains(phenotype), "phenotype should be in the set of possible alleles");
            assertTrue(allAlleles.contains(hiddenAllele), "hidden allele should be in the set of possible alleles");

            boolean validInheritance =
                    (parent1Genotype.contains(phenotype) && parent2Genotype.contains(hiddenAllele))
                            || (parent2Genotype.contains(phenotype) && parent1Genotype.contains(hiddenAllele));

            assertTrue(validInheritance,
                    "child alleles must be explainable as one allele from each parent");
        }
    }

    private Sheep createTestSheep(Map<Category, SheepGenotypeDTO> genotypes) {
        Sheep sheep = new Sheep();
        sheep.setGenotypes(genotypes);
        sheep.createDefaultDistributions();
        return sheep;
    }

    private String randomGrade() {
        return Grade.values()[random.nextInt(Grade.values().length)].code();
    }

    private String randomTone() {
        return Tone.values()[random.nextInt(Tone.values().length)].code();
    }
}
