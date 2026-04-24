package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.*;
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
public class SheepBackfillServiceTest {

    private final SheepBackfillService backfillService;
    private final SheepRepository sheepRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Autowired
    SheepBackfillServiceTest(
            SheepBackfillService backfillService,
            SheepRepository sheepRepository,
            JdbcTemplate jdbcTemplate,
            EntityManager entityManager
    ) {
        this.backfillService = backfillService;
        this.sheepRepository = sheepRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        ensureTestUserExists(DomainFixtures.TEST_USER_ID);
    }


    @Test
    void backfillMissingCategories_fillsMissingCategory() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.A.code(),
                Category.FLY, Grade.B.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.D.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        backfillService.backfillMissingCategories(false);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        assertNotNull(reloaded.getGenotype(Category.COLOR));
        assertEquals(Color.NORMAL, reloaded.getPhenotype(Category.COLOR));
        assertNotNull(reloaded.getDistribution(Category.COLOR, DistributionType.PRIOR));
    }


    @Test
    void backfillMissingCategories_doesNotOverwriteExistingCategory() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.A.code(),
                Category.FLY, Grade.B.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.D.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code(),
                Category.COLOR, Color.RED.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        backfillService.backfillMissingCategories(false);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        assertEquals(Color.RED, reloaded.getPhenotype(Category.COLOR));
    }


    @Test
    void backfillMissingCategories_dryRunDoesNotPersistChanges() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.A.code(),
                Category.FLY, Grade.B.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.D.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        backfillService.backfillMissingCategories(true);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        assertNull(reloaded.getGenotypeOrNull(Category.COLOR));
    }


    @Test
    void backfillMissingCategories_isIdempotent() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.A.code(),
                Category.FLY, Grade.B.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.D.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        backfillService.backfillMissingCategories(false);
        backfillService.backfillMissingCategories(false);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        assertNotNull(reloaded.getGenotype(Category.COLOR));
        assertEquals(Color.NORMAL, reloaded.getPhenotype(Category.COLOR));
    }


    @Test
    void backfillMissingCategories_initializesInferredDistribution() {
        Sheep sheep = DomainFixtures.createPartialTestSheep(Map.of(
                Category.SWIM, Grade.A.code(),
                Category.FLY, Grade.B.code(),
                Category.RUN, Grade.C.code(),
                Category.POWER, Grade.D.code(),
                Category.STAMINA, Grade.E.code(),
                Category.TONE, Tone.MONOTONE.code()
        ));

        sheepRepository.saveAndFlush(sheep);

        backfillService.backfillMissingCategories(false);

        entityManager.flush();
        entityManager.clear();

        Sheep reloaded = sheepRepository.findById(sheep.getId()).orElseThrow();

        Map<Color, Double> prior = reloaded.getDistribution(Category.COLOR, DistributionType.PRIOR);
        Map<Color, Double> inferred = reloaded.getDistribution(Category.COLOR, DistributionType.INFERRED);

        assertEquals(prior, inferred);
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
