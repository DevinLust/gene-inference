package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.BirthRecordRow;
import com.progressengine.geneinference.dto.BirthRecordSearchParams;
import com.progressengine.geneinference.dto.RelationshipRow;
import com.progressengine.geneinference.exception.BadRequestException;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.repository.BirthRecordRepository;
import com.progressengine.geneinference.repository.RelationshipRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;
    private final BirthRecordRepository birthRecordRepository;

    public RelationshipService(RelationshipRepository relationshipRepository, BirthRecordRepository birthRecordRepository) {
        this.relationshipRepository = relationshipRepository;
        this.birthRecordRepository = birthRecordRepository;
    }

    public Relationship findById(Integer id) {
        return relationshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Relationship with id " + id + " not found"
                ));
    }

    /**
     * Saves the given relationship in the database.
     *
     * @param relationship - relationship to save
     * @return the saved relationship
     */
    public Relationship saveRelationship(Relationship relationship) { return relationshipRepository.save(relationship); }

    /**
     * Fetches all relationships in the database
     *
     * @return a List of all relationships
     */
    public List<Relationship> getAllRelationships() {
        return relationshipRepository.findAllWithFullGraph();
    }

    public List<RelationshipRow>  getAllRelationshipRows(UUID userId) {
        return relationshipRepository.listAll(userId);
    }

    /**
     * Fetches the relationship with the given id, otherwise it throws an error.
     *
     * @param relationshipId - id of the desired relationship
     * @return the relationship with the given id
     */
    public Relationship getRelationshipById(Integer relationshipId) {
        Optional<Relationship> optionalRelationship = relationshipRepository.findById(relationshipId);
        if (optionalRelationship.isEmpty()) {
            throw new ResourceNotFoundException("Relationship not found");
        }
        return optionalRelationship.get();
    }

    /**
     * Fetches the relationship with the given id with birth records joined.
     *
     * @param relationshipId - id of the desired relationship
     * @return the relationship with the given id loaded with birth records
     */
    public Relationship getRelationshipWithBirthsById(UUID userId, Integer relationshipId) {
        Optional<Relationship> optionalRelationship = relationshipRepository.findWithBirthsById(userId, relationshipId);
        if (optionalRelationship.isEmpty()) {
            throw new ResourceNotFoundException("Relationship not found");
        }
        return optionalRelationship.get();
    }

    /**
     * Finds or creates a Relationship for the two given sheep. The relationship will automatically
     * set the first parent as the sheep with the lower id. If the relationship does not already
     * exist in the database it will return a new persisted Relationship. Throws an error if the
     * two sheep reference the same sheep.
     *
     * @param sheep1 - one of the two parent sheep
     * @param sheep2 - one of the two parent sheep
     * @return the relationship of these two sheep
     * @throws {@code IllegalArgumentException} if the two sheep are the same
     */
    public Relationship findOrCreateRelationship(Sheep sheep1, Sheep sheep2) {
        relationshipValidation(sheep1, sheep2); // validates these two sheep can be in relationship

        // Determine the lower and higher sheep IDs
        Integer sheepId1 = sheep1.getId();
        Integer sheepId2 = sheep2.getId();

        Integer parent1Id = Math.min(sheepId1, sheepId2);
        Sheep parent1 = sheepId1 < sheepId2 ? sheep1 : sheep2;
        Integer parent2Id = Math.max(sheepId1, sheepId2);
        Sheep parent2 = sheepId1 < sheepId2 ? sheep2 : sheep1;

        // Find existing relationship
        Optional<Relationship> existingRelationship = relationshipRepository
                .findByParent1_IdAndParent2_Id(parent1Id, parent2Id);

        if (existingRelationship.isPresent()) {
            return existingRelationship.get();
        }

        // Otherwise, create new
        Relationship newRelationship = new Relationship(parent1, parent2);

        return saveRelationship(newRelationship);
    }

    public List<Relationship> findRelationshipsByParent(Sheep parent) {
        return findRelationshipsByParent(parent.getId());
    }

    public List<Relationship> findRelationshipsByParent(Integer parentId) {
        return relationshipRepository.findByParentId(parentId);
    }

    public List<Relationship> findRelationshipsByParentWithUserId(UUID userId, Integer parentId) {
        return relationshipRepository.findByParentIdAndUserId(userId, parentId);
    }

    public List<List<Relationship>> filterRelationshipsByParent(Sheep parent1, Sheep parent2, int limitPerParent) {
        return filterRelationshipsByParent(parent1.getId(), parent2.getId(), limitPerParent);
    }

    public Page<BirthRecordRow> searchBirthRecords(BirthRecordSearchParams params, Pageable pageable) {
        Integer relId = params.relationshipId();

        // No relationship scope => don't allow parentsAtBirth filter
        if (relId == null) {
            if (params.hasAnyParentsAtBirth()) {
                throw new BadRequestException("parentsAtBirth filter requires relationshipId");
            }
            return birthRecordRepository.pageAllRows(pageable);
        }

        // Relationship scope, but filter not fully specified => return all for relationship
        if (!params.hasCompleteParentsAtBirth()) {
            return birthRecordRepository.pageRowsByParentRelationship(relId, pageable);
        }

        // Relationship scope + full filter => run filtered query
        return birthRecordRepository.pageRowsByParentPhenotypes(
                relId, params.category(), params.p1(), params.p2(), pageable
        );
    }

    /**
     * returns two lists of up to the given limit of relationships associated
     * with the corresponding parent order.
     *
     * @param parent1Id - id of the first parent
     * @param parent2Id - id of the second parent
     * @param limitPerParent - limit of number of relationships to find for each parent
     * @return a List containing two Lists of relationships, the List at index 0 are the
     * relationships of the first parent and the List at index 1 are the relationships of
     * the second parent.
     */
    public List<List<Relationship>> filterRelationshipsByParent(Integer parent1Id, Integer parent2Id, int limitPerParent) {
        List<Relationship> all = relationshipRepository.findLimitedByParents(parent1Id, parent2Id, limitPerParent);

        List<Relationship> parent1Relationships = new ArrayList<>();
        List<Relationship> parent2Relationships = new ArrayList<>();
        for (Relationship relationship : all) {
            if (relationship.getParent1().getId().equals(parent1Id) || relationship.getParent2().getId().equals(parent1Id)) {
                parent1Relationships.add(relationship);
            }
            if (relationship.getParent1().getId().equals(parent2Id) || relationship.getParent2().getId().equals(parent2Id)) {
                parent2Relationships.add(relationship);
            }
        }

        return List.of(parent1Relationships, parent2Relationships);
    }

    public void deleteAll(Collection<Relationship> relationships) {
        relationshipRepository.deleteAll(relationships);
    }

    public BirthRecord findBirthRecordById(Integer id) {
        return birthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birth record not found"));
    }

    @Transactional
    public void deleteBirthRecord(Integer birthRecordId) {
        BirthRecord br = findBirthRecordById(birthRecordId);
        Relationship rel = br.getParentRelationship(); // managed

        // If this birth record is linked to a saved child sheep, detach both sides
        Sheep child = br.getChild();
        if (child != null) {
            br.setChild(null);            // owning side
            child.setBirthRecord(null);   // inverse side
        }

        rel.removeBirthRecord(br);
        birthRecordRepository.deleteById(birthRecordId); // might not be needed
    }


    /** Validates that these two sheep can be in a relationship.
     *
     * @param sheep1 - the first parent sheep
     * @param sheep2 - the second parent sheep
     * @throws {@code IllegalArgumentException} if the two sheep are the same
     */
    private static void relationshipValidation(Sheep sheep1, Sheep sheep2) {
        if (sheep1 == sheep2 || sheep1.getId().equals(sheep2.getId())) {
            throw new IllegalArgumentException("Cannot breed a sheep with itself!");
        }
    }

}
