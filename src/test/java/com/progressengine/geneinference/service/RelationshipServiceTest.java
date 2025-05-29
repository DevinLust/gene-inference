package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Grade;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipServiceTest {
    @Test
    void testBreedNewSheep() {
        // Arrange
        Sheep parent1 = createTestSheep(Grade.A, Grade.C, SheepService.createUniformDistribution());

        Sheep parent2 = createTestSheep(Grade.B, Grade.S, SheepService.createUniformDistribution());

        Relationship relationship = createTestRelationship(parent1, parent2, Map.ofEntries(
                Map.entry(Grade.C, 1)
        ));

        // Act
        Sheep[] sheepChildren = new Sheep[10];
        for (int i = 0; i < sheepChildren.length; i++) {
            Sheep newChild = RelationshipService.breedNewSheep(relationship);
            sheepChildren[i] = newChild;
        }

        // Assert
        for (Sheep child : sheepChildren) {
            assertNotNull(child, "child should not be null");
            assertTrue(child.getPhenotype() == Grade.S ||  child.getPhenotype() == Grade.A || child.getPhenotype() == Grade.B || child.getPhenotype() == Grade.C, "phenotype should be S, A, B, or C");
            assertTrue(child.getHiddenAllele() == Grade.S ||  child.getHiddenAllele() == Grade.A || child.getHiddenAllele() == Grade.B || child.getHiddenAllele() == Grade.C, "hidden allele should be S, A, B, or C");
            if (child.getPhenotype() == Grade.A ||  child.getPhenotype() == Grade.C) {
                assertTrue(child.getHiddenAllele() == Grade.B || child.getHiddenAllele() == Grade.S, "hidden allele should be from parent 2");
            } else {
                assertTrue(child.getHiddenAllele() == Grade.A || child.getHiddenAllele() == Grade.C, "hidden allele should be from parent 1");
            }
        }
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
