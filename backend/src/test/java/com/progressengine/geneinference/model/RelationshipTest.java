package com.progressengine.geneinference.model;

import com.progressengine.geneinference.exception.ExcessAlleleDiversityException;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.model.enums.Tone;
import com.progressengine.geneinference.testutil.DomainFixtures;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipTest {
    @Test
    public void testAddChildToRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
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
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
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
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship rel1 = DomainFixtures.createEmptyRelationship(parent1, parent2);

        // Arrange
        Sheep parent3 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        Sheep parent4 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship rel2 = DomainFixtures.createEmptyRelationship(parent3, parent4);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
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
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.MONOTONE.code()
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
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        relationship.addChildToRelationship(child);

        // Act/Assert
        assertThrows(IllegalStateException.class, () -> relationship.addChildInformationToRelationship(child));
    }

    @Test
    public void testRemoveChildFromRelationship() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
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
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.C.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.A.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Relationship relationship = DomainFixtures.createEmptyRelationship(parent1, parent2);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.C.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.B.code(),
                Category.STAMINA, Grade.C.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));

        // Act/Assert
        assertThrows(IllegalStateException.class, () -> relationship.removeChildFromRelationship(child));
    }

    @Test
    public void testFrequencyCache() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.TWO_TONE.code()
        ));
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> expectedPhenotypeFrequencies = Map.ofEntries(
                Map.entry(Category.SWIM, Map.of(
                        new AlleleCodePair(Grade.B, Grade.B), Map.ofEntries(
                                Map.entry(Grade.B.code(), 53),
                                Map.entry(Grade.C.code(), 24),
                                Map.entry(Grade.D.code(), 23)
                        )
                )),
                Map.entry(Category.FLY, Map.of(
                        new AlleleCodePair(Grade.A.code(), Grade.A.code()), Map.ofEntries(
                                Map.entry(Grade.A.code(), 50),
                                Map.entry(Grade.C.code(), 50)
                        )
                )),
                Map.entry(Category.RUN, Map.of(
                        new AlleleCodePair(Grade.C, Grade.D), Map.ofEntries(
                                Map.entry(Grade.S.code(), 25),
                                Map.entry(Grade.A.code(), 25),
                                Map.entry(Grade.C.code(), 26),
                                Map.entry(Grade.D.code(), 24)
                        )
                )),
                Map.entry(Category.POWER, Map.of(
                        new AlleleCodePair(Grade.S, Grade.C), Map.ofEntries(
                                Map.entry(Grade.S.code(), 53),
                                Map.entry(Grade.C.code(), 47)
                        )
                )),
                Map.entry(Category.STAMINA, Map.of(
                        new AlleleCodePair(Grade.E, Grade.S), Map.ofEntries(
                                Map.entry(Grade.S.code(), 25),
                                Map.entry(Grade.E.code(), 75)
                        )
                )),
                Map.entry(Category.TONE, Map.of(
                        new AlleleCodePair(Tone.TWO_TONE, Tone.TWO_TONE), Map.ofEntries(
                                Map.entry(Tone.TWO_TONE.code(), 52),
                                Map.entry(Tone.MONOTONE.code(), 48)
                        )
                ))
        );
        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, expectedPhenotypeFrequencies);

        // Act
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeCache = relationship.getPhenotypeFrequencies();

        // Assert
        assertEquals(
                withEmptyFrequencyCategories(expectedPhenotypeFrequencies),
                phenotypeCache,
                "Cache should equal phenotype frequencies"
        );
    }

    @Test
    public void testAddChildToRelationshipExcessiveAlleles() {
        // Arrange
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.S.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.ofEntries(
                Map.entry(Category.SWIM, Map.of(
                        new AlleleCodePair(Grade.B, Grade.B), Map.ofEntries(
                                Map.entry(Grade.B.code(), 53),
                                Map.entry(Grade.C.code(), 24),
                                Map.entry(Grade.D.code(), 23)
                        )
                )),
                Map.entry(Category.FLY, Map.of(
                        new AlleleCodePair(Grade.A, Grade.A), Map.ofEntries(
                                Map.entry(Grade.A.code(), 50),
                                Map.entry(Grade.C.code(), 50)
                        )
                )),
                Map.entry(Category.RUN, Map.of(
                        new AlleleCodePair(Grade.C, Grade.D), Map.ofEntries(
                                Map.entry(Grade.S.code(), 25),
                                Map.entry(Grade.A.code(), 25),
                                Map.entry(Grade.C.code(), 26),
                                Map.entry(Grade.D.code(), 24)
                        )
                )),
                Map.entry(Category.POWER, Map.of(
                        new AlleleCodePair(Grade.S, Grade.C), Map.ofEntries(
                                Map.entry(Grade.S.code(), 53),
                                Map.entry(Grade.C.code(), 47)
                        )
                )),
                Map.entry(Category.STAMINA, Map.of(
                        new AlleleCodePair(Grade.E, Grade.S), Map.ofEntries(
                                Map.entry(Grade.S.code(), 25),
                                Map.entry(Grade.E.code(), 75)
                        )
                )),
                Map.entry(Category.TONE, Map.of(
                        new AlleleCodePair(Tone.MONOTONE, Tone.MONOTONE), Map.ofEntries(
                                Map.entry(Tone.TWO_TONE.code(), 25),
                                Map.entry(Tone.MONOTONE.code(), 75)
                        )
                ))
        );
        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child =  DomainFixtures.createTestSheep(Map.of(
                Category.SWIM, Grade.S.code(),
                Category.FLY, Grade.A.code(),
                Category.RUN, Grade.D.code(),
                Category.POWER, Grade.C.code(),
                Category.STAMINA, Grade.S.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        // Act/Assert
        assertThrows(ExcessAlleleDiversityException.class, () -> relationship.addChildToRelationship(child));
    }

    @Test
    public void testAddChildToRelationship_colorHistoryCanForceParentHiddenAssignment() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "NRM"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "RED"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.COLOR, Map.of(
                        new AlleleCodePair("NRM", "RED"),
                        Map.of("BLU", 1)
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "NRM"
        ));

        assertThrows(
                ExcessAlleleDiversityException.class,
                () -> relationship.addChildToRelationship(child)
        );
    }

    @Test
    public void testAddChildToRelationship_colorConflictingHistoryRejectedEvenIfEarlierBirthMayBeWrong() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "NRM"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "RED"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.COLOR, Map.of(
                        new AlleleCodePair("NRM", "RED"),
                        Map.of("PUR", 1)
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "BLU"
        ));

        assertThrows(
                ExcessAlleleDiversityException.class,
                () -> relationship.addChildToRelationship(child)
        );
    }

    @Test
    public void testAddChildToRelationship_colorRecessivePhenotypeCountsAsStrongConstraint() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "NRM"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "RED"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.COLOR, Map.of(
                        new AlleleCodePair("NRM", "RED"),
                        Map.of("NRM", 1)
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "BLU"
        ));

        assertThrows(
                ExcessAlleleDiversityException.class,
                () -> relationship.addChildToRelationship(child)
        );
    }

    @Test
    public void testAddChildToRelationship_colorTwoWitnessedHiddenAllelesAllowedWhenSplitAcrossParents() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "RED"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "GRN"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.COLOR, Map.of(
                        new AlleleCodePair("RED", "GRN"),
                        Map.of(
                                "BLU", 1,
                                "PUR", 1
                        )
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "BLU"
        ));

        assertDoesNotThrow(() -> relationship.addChildToRelationship(child));
    }

    @Test
    public void testAddChildToRelationship_colorThreeHiddenAllelesAlwaysViolates() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "RED"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "GRN"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.COLOR, Map.of(
                        new AlleleCodePair("RED", "GRN"),
                        Map.of(
                                "BLU", 1,
                                "PUR", 1
                        )
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.COLOR, "YEL"
        ));

        assertThrows(
                ExcessAlleleDiversityException.class,
                () -> relationship.addChildToRelationship(child)
        );
    }

    @Test
    public void testAddChildToRelationship_shinyRecessivePhenotypeCountsAsStrongConstraint() {
        Sheep parent1 = DomainFixtures.createTestSheep(Map.of(
                Category.SHINY, "NRM"
        ));
        Sheep parent2 = DomainFixtures.createTestSheep(Map.of(
                Category.SHINY, "NRM"
        ));

        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> phenotypeFrequencies = Map.of(
                Category.SHINY, Map.of(
                        new AlleleCodePair("NRM", "NRM"),
                        Map.of("NRM", 1)
                )
        );

        Relationship relationship = DomainFixtures.createPopulatedRelationship(parent1, parent2, phenotypeFrequencies);

        Sheep child = DomainFixtures.createTestSheep(Map.of(
                Category.SHINY, "SHN"
        ));

        // Depending on your exact shiny-domain rules, either this should succeed or fail.
        // The point is to assert the rule explicitly once you settle the semantics.
        assertThrows(
                ExcessAlleleDiversityException.class,
                () -> relationship.addChildToRelationship(child)
        );
    }

    private static Map<Category, Map<AlleleCodePair, Map<String, Integer>>> withEmptyFrequencyCategories(
            Map<Category, Map<AlleleCodePair, Map<String, Integer>>> input
    ) {
        Map<Category, Map<AlleleCodePair, Map<String, Integer>>> result = new EnumMap<>(Category.class);

        for (Category category : Category.values()) {
            Map<AlleleCodePair, Map<String, Integer>> categoryMap =
                    input.getOrDefault(category, Map.of());

            Map<AlleleCodePair, Map<String, Integer>> categoryCopy = new HashMap<>();
            for (Map.Entry<AlleleCodePair, Map<String, Integer>> entry : categoryMap.entrySet()) {
                categoryCopy.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }

            result.put(category, categoryCopy);
        }

        return result;
    }
}
