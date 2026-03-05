package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.testutil.DomainFixtures;
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
        UUID userId = DomainFixtures.TEST_USER_ID;
        Sheep parent1 = DomainFixtures.createTestSheep(userId, Map.of(
                Category.SWIM, randomGrade(),
                Category.FLY, randomGrade(),
                Category.RUN, randomGrade(),
                Category.POWER, randomGrade(),
                Category.STAMINA, randomGrade()
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(userId, Map.of(
                Category.SWIM, randomGrade(),
                Category.FLY, randomGrade(),
                Category.RUN, randomGrade(),
                Category.POWER, randomGrade(),
                Category.STAMINA, randomGrade()
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
        Relationship savedRelationship = relationshipService.findOrCreateRelationship(userId, parent2, parent1);

        // Assert
        assertEquals(parent1Id, savedRelationship.getParent1().getId());
        assertEquals(parent2Id, savedRelationship.getParent2().getId());
    }

    private Grade randomGrade() {
        return Grade.values()[random.nextInt(Grade.values().length)];
    }
}
