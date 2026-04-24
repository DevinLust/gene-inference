package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.dto.SheepUpdateRequestDTO;
import com.progressengine.geneinference.model.AlleleCodePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Color;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import com.progressengine.geneinference.testutil.DomainFixtures;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SheepServiceUpdateSheepTest {

    private final SheepService sheepService;
    private final SheepRepository sheepRepository;
    private final RelationshipRepository relationshipRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Autowired
    SheepServiceUpdateSheepTest(
            SheepService sheepService,
            SheepRepository sheepRepository,
            RelationshipRepository relationshipRepository,
            JdbcTemplate jdbcTemplate,
            EntityManager entityManager
    ) {
        this.sheepService = sheepService;
        this.sheepRepository = sheepRepository;
        this.relationshipRepository = relationshipRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        ensureTestUserExists(DomainFixtures.TEST_USER_ID);
    }

    @Test
    void updateSheep_allowsUpdatingCategoryWithoutHistoricalData() {
        Sheep parent1 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Sheep parent2 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Relationship relationship = DomainFixtures.createPopulatedRelationship(
                parent1,
                parent2,
                Map.of(
                        Category.SWIM, Map.of(
                                new AlleleCodePair(Grade.B, Grade.B),
                                Map.of(Grade.B.code(), 1)
                        )
                )
        );

        sheepRepository.saveAndFlush(parent1);
        sheepRepository.saveAndFlush(parent2);
        relationshipRepository.saveAndFlush(relationship);

        SheepUpdateRequestDTO dto = new SheepUpdateRequestDTO();
        dto.setGenotypes(Map.of(
                Category.COLOR,
                new SheepGenotypeDTO(Color.RED.code(), Color.NORMAL.code())
        ));

        Sheep updated = sheepService.updateSheep(DomainFixtures.TEST_USER_ID, parent1.getId(), dto);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(updated.getId()).orElseThrow();

        assertEquals(Color.RED, reloaded.getPhenotype(Category.COLOR));
        assertEquals(Color.NORMAL, reloaded.getHiddenAllele(Category.COLOR));
    }


    @Test
    void updateSheep_rejectsUpdatingCategoryWithHistoricalData() {
        Sheep parent1 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Sheep parent2 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Relationship relationship = DomainFixtures.createPopulatedRelationship(
                parent1,
                parent2,
                Map.of(
                        Category.SWIM, Map.of(
                                new AlleleCodePair(Grade.B, Grade.B),
                                Map.of(Grade.B.code(), 1)
                        )
                )
        );

        sheepRepository.saveAndFlush(parent1);
        sheepRepository.saveAndFlush(parent2);
        relationshipRepository.saveAndFlush(relationship);

        SheepUpdateRequestDTO dto = new SheepUpdateRequestDTO();
        dto.setGenotypes(Map.of(
                Category.SWIM,
                new SheepGenotypeDTO(Grade.A.code(), Grade.S.code())
        ));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> sheepService.updateSheep(DomainFixtures.TEST_USER_ID, parent1.getId(), dto)
        );

        assertTrue(ex.getMessage().contains("SWIM"));
    }


    @Test
    void updateSheep_allowedEditResyncsPriorAndInferred() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.COLOR, Color.NORMAL.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        SheepUpdateRequestDTO dto = new SheepUpdateRequestDTO();
        dto.setGenotypes(Map.of(
                Category.COLOR,
                new SheepGenotypeDTO(Color.NORMAL.code(), null)
        ));

        sheepService.updateSheep(DomainFixtures.TEST_USER_ID, sheep.getId(), dto);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        Map<Color, Double> prior = reloaded.getDistribution(Category.COLOR, DistributionType.PRIOR);
        Map<Color, Double> inferred = reloaded.getDistribution(Category.COLOR, DistributionType.INFERRED);

        assertEquals(1.0, prior.get(Color.NORMAL));
        assertEquals(prior, inferred);
        assertEquals(Color.NORMAL, reloaded.getHiddenAllele(Category.COLOR));
    }


    @Test
    void updateSheep_mixedLockedAndUnlockedCategoriesDoesNotPartiallyApply() {
        Sheep parent1 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Sheep parent2 = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.B.code(),
                Category.COLOR, Color.NORMAL.code()
        ));

        Relationship relationship = DomainFixtures.createPopulatedRelationship(
                parent1,
                parent2,
                Map.of(
                        Category.SWIM, Map.of(
                                new AlleleCodePair(Grade.B, Grade.B),
                                Map.of(Grade.B.code(), 1)
                        )
                )
        );

        sheepRepository.saveAndFlush(parent1);
        sheepRepository.saveAndFlush(parent2);
        relationshipRepository.saveAndFlush(relationship);

        SheepUpdateRequestDTO dto = new SheepUpdateRequestDTO();
        dto.setGenotypes(Map.of(
                Category.SWIM, new SheepGenotypeDTO(Grade.A.code(), Grade.S.code()),
                Category.COLOR, new SheepGenotypeDTO(Color.RED.code(), Color.NORMAL.code())
        ));

        assertThrows(
                IllegalStateException.class,
                () -> sheepService.updateSheep(DomainFixtures.TEST_USER_ID, parent1.getId(), dto)
        );

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(parent1.getId()).orElseThrow();

        assertEquals(Grade.B, reloaded.getPhenotype(Category.SWIM));
        assertEquals(Color.NORMAL, reloaded.getPhenotype(Category.COLOR));
    }


    private void ensureTestUserExists(UUID userId) {
        jdbcTemplate.update("""
        insert into auth.users (
            id,
            aud,
            role,
            email,
            encrypted_password,
            email_confirmed_at,
            raw_app_meta_data,
            raw_user_meta_data,
            created_at,
            updated_at
        )
        values (
            ?,
            'authenticated',
            'authenticated',
            ?,
            '',
            now(),
            '{}'::jsonb,
            '{}'::jsonb,
            now(),
            now()
        )
        on conflict (id) do nothing
        """,
                userId,
                "test-" + userId + "@example.com"
        );

        jdbcTemplate.update("""
        insert into profiles (id)
        values (?)
        on conflict (id) do nothing
        """,
                userId
        );
    }
}
