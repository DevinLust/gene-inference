package com.progressengine.geneinference.model;

import com.progressengine.geneinference.exception.ExcessAlleleDiversityException;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.testutil.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipTest {
    @Test
    public void testAddChildToRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));

        // Act
        BirthRecord birthRecord = relationship.addChildToRelationship(child);

        // Assert
        assertNotNull(birthRecord, "Birth record should not be null");
        assertTrue(relationship.getBirthRecords().contains(birthRecord), "Birth record should be added");
        assertSame(relationship, birthRecord.getParentRelationship(), "Parent relationship should be same");
        assertSame(child, birthRecord.getChild(), "Birth record should have reference to child");
        assertSame(birthRecord, child.getBirthRecord(), "Child should have reference to birth record");
    }

    @Test
    public void testAddChildToRelationshipAlreadyAChild() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));
        BirthRecord birthRecord = relationship.addChildToRelationship(child);

        // Act
        BirthRecord testBr = relationship.addChildToRelationship(child);

        // Assert
        assertSame(birthRecord, testBr, "Birth records should be same");
        assertEquals(1, relationship.getBirthRecords().size(), "Birth record list shouldn't change");
    }

    @Test
    public void testAddChildToRelationshipChildOfDifferentRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship rel1 = DomainFixtures.createEmptyRelationship(parent1, parent2);

        // Arrange
        Sheep parent3 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent4 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship rel2 = DomainFixtures.createEmptyRelationship(parent3, parent4);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));

        // Act
        rel1.addChildToRelationship(child);

        // Assert
        assertThrows(IllegalStateException.class, () -> rel2.addChildToRelationship(child));
    }

    @Test
    public void testAddChildInformationToRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));

        // Act
        BirthRecord birthRecord = relationship.addChildInformationToRelationship(child);

        // Assert
        assertNotNull(birthRecord, "Birth record should not be null");
        assertTrue(relationship.getBirthRecords().contains(birthRecord), "Birth record should be added");
        assertSame(relationship, birthRecord.getParentRelationship(), "Parent relationship should be same as the original relationship");
        assertNull(birthRecord.getChild(), "Birth record should not reference child");
        assertNull(child.getBirthRecord(), "Child should not reference birth record");
    }

    @Test
    public void testAddChildInformationToRelationshipAlreadyAChild() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));
        relationship.addChildToRelationship(child);

        // Act/Assert
        assertThrows(IllegalStateException.class, () -> relationship.addChildInformationToRelationship(child));
    }

    @Test
    public void testRemoveChildFromRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));
        BirthRecord birthRecord = relationship.addChildToRelationship(child);

        // Act
        relationship.removeChildFromRelationship(child);

        // Assert
        assertFalse(relationship.getBirthRecords().contains(birthRecord), "Birth record should be removed");
        assertNull(birthRecord.getParentRelationship(), "Birth record should not reference parent");
        assertNull(birthRecord.getChild(), "Birth record should not reference child");
        assertNull(child.getBirthRecord(), "Child should not reference birth record");
    }

    @Test
    public void testRemoveChildFromRelationshipChildNotInRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.C,
                Category.RUN, Grade.D,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.A,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.B,
                Category.STAMINA, Grade.C
        ));

        // Act/Assert
        assertThrows(IllegalStateException.class, () -> relationship.removeChildFromRelationship(child));
    }

    @Test
    public void testFrequencyCache() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.C,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Map<Category, Map<GradePair, Map<Grade, Integer>>> expectedPhenotypeFrequencies = Map.ofEntries(
                Map.entry(Category.SWIM, Map.of(
                        new GradePair(Grade.B, Grade.B), Map.ofEntries(
                                Map.entry(Grade.B, 53),
                                Map.entry(Grade.C, 24),
                                Map.entry(Grade.D, 23)
                        )
                )),
                Map.entry(Category.FLY, Map.of(
                        new GradePair(Grade.A, Grade.A), Map.ofEntries(
                                Map.entry(Grade.A, 50),
                                Map.entry(Grade.C, 50)
                        )
                )),
                Map.entry(Category.RUN, Map.of(
                        new GradePair(Grade.C, Grade.D), Map.ofEntries(
                                Map.entry(Grade.S, 25),
                                Map.entry(Grade.A, 25),
                                Map.entry(Grade.C, 26),
                                Map.entry(Grade.D, 24)
                        )
                )),
                Map.entry(Category.POWER, Map.of(
                        new GradePair(Grade.S, Grade.C), Map.ofEntries(
                                Map.entry(Grade.S, 53),
                                Map.entry(Grade.C, 47)
                        )
                )),
                Map.entry(Category.STAMINA, Map.of(
                        new GradePair(Grade.E, Grade.S), Map.ofEntries(
                                Map.entry(Grade.S, 25),
                                Map.entry(Grade.E, 75)
                        )
                ))
        );
        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, expectedPhenotypeFrequencies);

        // Act
        Map<Category, Map<GradePair, Map<Grade, Integer>>> phenotypeCache = relationship.getPhenotypeFrequencies();

        // Assert
        assertEquals(expectedPhenotypeFrequencies, phenotypeCache, "Cache should equal phenotype frequencies");
    }

    @Test
    public void testAddChildToRelationshipExcessiveAlleles() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.C,
                Category.POWER, Grade.S,
                Category.STAMINA, Grade.E
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));
        Map<Category, Map<GradePair, Map<Grade, Integer>>> phenotypeFrequencies = Map.ofEntries(
                Map.entry(Category.SWIM, Map.of(
                        new GradePair(Grade.B, Grade.B), Map.ofEntries(
                                Map.entry(Grade.B, 53),
                                Map.entry(Grade.C, 24),
                                Map.entry(Grade.D, 23)
                        )
                )),
                Map.entry(Category.FLY, Map.of(
                        new GradePair(Grade.A, Grade.A), Map.ofEntries(
                                Map.entry(Grade.A, 50),
                                Map.entry(Grade.C, 50)
                        )
                )),
                Map.entry(Category.RUN, Map.of(
                        new GradePair(Grade.C, Grade.D), Map.ofEntries(
                                Map.entry(Grade.S, 25),
                                Map.entry(Grade.A, 25),
                                Map.entry(Grade.C, 26),
                                Map.entry(Grade.D, 24)
                        )
                )),
                Map.entry(Category.POWER, Map.of(
                        new GradePair(Grade.S, Grade.C), Map.ofEntries(
                                Map.entry(Grade.S, 53),
                                Map.entry(Grade.C, 47)
                        )
                )),
                Map.entry(Category.STAMINA, Map.of(
                        new GradePair(Grade.E, Grade.S), Map.ofEntries(
                                Map.entry(Grade.S, 25),
                                Map.entry(Grade.E, 75)
                        )
                ))
        );
        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child =  DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.S,
                Category.FLY, Grade.A,
                Category.RUN, Grade.D,
                Category.POWER, Grade.C,
                Category.STAMINA, Grade.S
        ));

        // Act/Assert
        assertThrows(ExcessAlleleDiversityException.class, () -> relationship.addChildToRelationship(child));
    }
}
