package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @InjectMocks
    private RelationshipService relationshipService;

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

        Sheep parent1 = createTestSheep(parent1Phenotype, parent1HiddenAllele, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(parent2Phenotype, parent2HiddenAllele, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, new EnumMap<>(Grade.class));

        // Act
        Sheep[] sheepChildren = new Sheep[10];
        for (int i = 0; i < sheepChildren.length; i++) {
            Sheep newChild = RelationshipService.breedNewSheep(relationship);
            sheepChildren[i] = newChild;
        }

        // Assert
        for (Sheep child : sheepChildren) {
            assertNotNull(child, "child should not be null");
            assertTrue(allAlleles.contains(child.getPhenotype()), "phenotype should be in the set of possible alleles");
            assertTrue(allAlleles.contains(child.getHiddenAllele()), "hidden allele should be in the set of possible alleles");
            if (parent1Genotype.contains(child.getPhenotype())) {
                assertTrue(parent2Genotype.contains(child.getHiddenAllele()), "hidden allele should be from parent 2");
            } else if (parent2Genotype.contains(child.getPhenotype())) {
                assertTrue(parent1Genotype.contains(child.getHiddenAllele()), "hidden allele should be from parent 1");
            } else {
                fail("impossible phenotype: " + child.getPhenotype());
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

        Sheep parent1 = createTestSheep(parent1Phenotype, parent1HiddenAllele, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(parent2Phenotype, parent2HiddenAllele, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, new EnumMap<>(Grade.class));

        // Act
        for (int i = 0; i < N; i++) {
            RelationshipService.breedNewSheep(relationship);
        }

        // Assert for distribution
        Map<Grade, Integer> counts = relationship.getOffspringPhenotypeFrequency();
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

    @Test
    void testFindOrCreateRelationship() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.S, Grade.S, null);
        Sheep parent2 = createTestSheep(Grade.E, Grade.E, null);

        int parent1Id = 1;
        int parent2Id = 2;
        parent1.setId(parent1Id);
        parent2.setId(parent2Id);

        when(relationshipRepository.findByParent1_IdAndParent2_Id(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        when(relationshipRepository.save(any(Relationship.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Relationship savedRelationship = relationshipService.findOrCreateRelationship(parent2, parent1);

        // Assert
        assertEquals(parent1Id, savedRelationship.getParent1().getId());
        assertEquals(parent2Id, savedRelationship.getParent2().getId());
    }

    private Sheep createTestSheep(Grade phenotype, Grade hiddenAllele, Map<Grade, Double> hiddenDistribution) {
        Sheep sheep = new Sheep();
        sheep.setPhenotype(phenotype);
        sheep.setHiddenAllele(hiddenAllele);
        sheep.setHiddenDistribution(hiddenDistribution);
        return sheep;
    }

    private Relationship createTestRelationship(Sheep parent1, Sheep parent2, Map<Grade, Integer> offspringPhenotypeFrequency) {
        Relationship relationship = new Relationship();
        relationship.setParent1(parent1);
        relationship.setParent2(parent2);
        relationship.setOffspringPhenotypeFrequency(offspringPhenotypeFrequency);
        return relationship;
    }
}
