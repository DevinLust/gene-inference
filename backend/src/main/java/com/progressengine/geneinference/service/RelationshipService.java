package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public RelationshipService(RelationshipRepository relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
    }

    public Relationship saveRelationship(Relationship relationship) { return relationshipRepository.save(relationship); }

    public List<Relationship> getAllRelationships() {
        return relationshipRepository.findAll();
    }

    /**
     * Finds or creates a Relationship for the two sheep.
     */
    public Relationship findOrCreateRelationship(Sheep sheep1, Sheep sheep2) {
        // Determine the lower and higher sheep IDs
        Integer sheepId1 = sheep1.getId();
        Integer sheepId2 = sheep2.getId();

        if (sheep1 == sheep2 || sheepId1.equals(sheepId2)) {
            throw new IllegalArgumentException("Cannot breed a sheep with itself!");
        }

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
        Relationship newRelationship = new Relationship();
        newRelationship.setParent1(parent1);
        newRelationship.setParent2(parent2);

        // set other fields as needed

        return relationshipRepository.save(newRelationship);
    }

    public List<Relationship> findRelationshipsByParent(Sheep parent) {
        return findRelationshipsByParent(parent.getId());
    }

    public List<Relationship> findRelationshipsByParent(Integer parentId) {
        return relationshipRepository.findByParentId(parentId);
    }

    public List<List<Relationship>> filterRelationshipsByParent(Sheep parent1, Sheep parent2, int limitPerParent) {
        return filterRelationshipsByParent(parent1.getId(), parent2.getId(), limitPerParent);
    }

    // returns two lists of up to the given limit of relationships associated with the corresponding parent order.
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

    // assumes uniform distribution for child
    public static Sheep breedNewSheep(Relationship relationship) {
        Sheep child = new Sheep();

        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        // random genotype from the two parents, one allele from each parent
        Random random = new Random();
        // -----------------------------------------------------------------------------------
        for (Category category : Category.values()) {
            if (parent1.getHiddenAllele(category) == null || parent2.getHiddenAllele(category) == null) {
                throw new IllegalArgumentException("Missing known hidden allele in category " + category);
            }
            Grade newPhenotype;
            Grade newHiddenAllele;
            if (random.nextBoolean()) {
                newPhenotype = random.nextBoolean() ? parent1.getPhenotype(category) : parent1.getHiddenAllele(category);
                newHiddenAllele = random.nextBoolean() ? parent2.getPhenotype(category) : parent2.getHiddenAllele(category);
            } else {
                newPhenotype = random.nextBoolean() ? parent2.getPhenotype(category) : parent2.getHiddenAllele(category);
                newHiddenAllele = random.nextBoolean() ? parent1.getPhenotype(category) : parent1.getHiddenAllele(category);
            }


            child.setPhenotype(category, newPhenotype);
            child.setHiddenAllele(category, newHiddenAllele);
            relationship.updatePhenotypeFrequency(category, newPhenotype, 1);
        }
        child.createDefaultDistributions();
        // ---------------------------------------------------------------------------------

        child.setParentRelationship(relationship);

        return child;
    }

    public RelationshipResponseDTO toResponseDTO(Relationship relationship) {
        return new RelationshipResponseDTO(relationship);
    }
}
