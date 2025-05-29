package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public RelationshipService(RelationshipRepository relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
    }

    /**
     * Finds or creates a Relationship for the two sheep.
     */
    public Relationship findOrCreateRelationship(Sheep sheep1, Sheep sheep2) {
        // Determine the lower and higher sheep IDs
        Integer sheepId1 = sheep1.getId();
        Integer sheepId2 = sheep2.getId();

        Integer parent1Id = Math.min(sheepId1, sheepId2);
        Integer parent2Id = Math.max(sheepId1, sheepId2);

        // Find existing relationship
        Optional<Relationship> existingRelationship = relationshipRepository
                .findByParent1_IdAndParent2_Id(parent1Id, parent2Id);

        if (existingRelationship.isPresent()) {
            return existingRelationship.get();
        }

        // Otherwise, create new
        Relationship newRelationship = new Relationship();
        newRelationship.setParent1(sheep1);
        newRelationship.setParent2(sheep2);
        // set other fields as needed

        return relationshipRepository.save(newRelationship);
    }

    // assumes uniform distribution for child
    public static Sheep breedNewSheep(Relationship relationship) {
        Sheep child = new Sheep();

        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();

        // random genotype from the two parents, one allele from each parent
        Random random = new Random();
        Grade newPhenotype = random.nextBoolean() ? parent2.getPhenotype() : parent2.getHiddenAllele();
        Grade newHiddenAllele = random.nextBoolean() ? parent1.getPhenotype() : parent1.getHiddenAllele();
        if (random.nextBoolean()) {
            newPhenotype = random.nextBoolean() ? parent1.getPhenotype() : parent1.getHiddenAllele();
            newHiddenAllele =  random.nextBoolean() ? parent2.getPhenotype() : parent2.getHiddenAllele();
        }

        child.setPhenotype(newPhenotype);
        child.setHiddenAllele(newHiddenAllele);

        child.setHiddenDistribution(SheepService.createUniformDistribution());

        child.setParentRelationship(relationship);

        return child;
    }
}
