package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
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

    private static final Random random = new Random(42);

    @Mock
    private RelationshipRepository relationshipRepository;

    @InjectMocks
    private RelationshipService relationshipService;


    @Test
    void testFindOrCreateRelationship() {
        // Arrange
        Sheep parent1 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(Grade.S, Grade.S),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));
        Sheep parent2 = createTestSheep(Map.of(
                Category.SWIM, new SheepGenotypeDTO(Grade.E, Grade.E),
                Category.FLY, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.RUN, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.POWER, new SheepGenotypeDTO(randomGrade(), randomGrade()),
                Category.STAMINA, new SheepGenotypeDTO(randomGrade(), randomGrade())
        ));

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
