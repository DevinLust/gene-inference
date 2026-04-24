package com.progressengine.geneinference.integration;

import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.BirthRecordPhenotype;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import com.progressengine.geneinference.service.BreedingService;
import com.progressengine.geneinference.testutil.DomainFixtures;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BreedingServiceIntegrationTest {

    private final BreedingService breedingService;
    private final SheepRepository sheepRepository;
    private final RelationshipRepository relationshipRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final UUID TEST_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    BreedingServiceIntegrationTest(
            BreedingService breedingService,
            SheepRepository sheepRepository,
            RelationshipRepository relationshipRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.breedingService = breedingService;
        this.sheepRepository = sheepRepository;
        this.relationshipRepository = relationshipRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUpUser() {
        jdbcTemplate.update("""
            insert into auth.users (id)
            values (?)
            on conflict (id) do nothing
            """, TEST_USER_ID);

        jdbcTemplate.update("""
            insert into profiles (id)
            values (?)
            on conflict (id) do nothing
            """, TEST_USER_ID);
    }

    @Test
    void recalculateAll_handlesMixedPartialAndFullBirthRecords() {
        Sheep oldP1 = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));
        Sheep oldP2 = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));
        Sheep oldChild = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));

        Relationship oldRelationship = new Relationship();
        oldRelationship.setParent1(oldP1);
        oldRelationship.setParent2(oldP2);

        BirthRecord oldRecord = partialBirthRecord(
                oldRelationship,
                oldChild,
                Category.SWIM,
                Category.FLY,
                Category.RUN,
                Category.POWER,
                Category.STAMINA
        );

        oldRelationship.addBirthRecord(oldRecord);
        relationshipRepository.save(oldRelationship);

        Sheep newP1 = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));
        Sheep newP2 = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));
        Sheep newChild = sheepRepository.save(DomainFixtures.createTestSheep(TEST_USER_ID, Map.of()));

        Relationship newRelationship = new Relationship();
        newRelationship.setParent1(newP1);
        newRelationship.setParent2(newP2);

        BirthRecord fullRecord = BirthRecord.create(
                newRelationship,
                newChild.getGenotypes(),
                newChild
        );

        newRelationship.addBirthRecord(fullRecord);
        relationshipRepository.save(newRelationship);

        assertDoesNotThrow(() -> {
            List<Map<Category, Map<String, Double>>> beliefs =
                    breedingService.recalculateAll(TEST_USER_ID);

            assertNotNull(beliefs);
            assertFalse(beliefs.isEmpty());
        });
    }

    private BirthRecord partialBirthRecord(
            Relationship relationship,
            Sheep child,
            Category... categories
    ) {
        BirthRecord record = new BirthRecord();
        record.setParentRelationship(relationship);
        record.setChild(child);

        for (Category category : categories) {
            BirthRecordPhenotype phenotypeRecord = new BirthRecordPhenotype(record, category);
            phenotypeRecord.setAllPhenotypeCodes(
                    relationship.getParent1().getPhenotype(category).code(),
                    relationship.getParent2().getPhenotype(category).code(),
                    child.getPhenotype(category).code()
            );
            record.getPhenotypesAtBirth().add(phenotypeRecord);
        }

        child.setBirthRecord(record);
        return record;
    }
}

