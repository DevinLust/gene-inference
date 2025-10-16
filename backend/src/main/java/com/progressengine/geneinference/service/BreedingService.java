package com.progressengine.geneinference.service;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class BreedingService {
    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final InferenceEngine inferenceEngine;

    public BreedingService(SheepService sheepService, RelationshipService relationshipService, @Qualifier("loopy") InferenceEngine inferenceEngine) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.inferenceEngine = inferenceEngine;
    }

    @Transactional
    public void saveBreedingSession(Relationship relationshipToSave, List<Sheep> sheepToSave) {
        relationshipService.saveRelationship(relationshipToSave);
        sheepService.saveAll(sheepToSave);
    }

    @Transactional
    public void saveBreedingSession(Relationship relationshipToSave, Sheep sheepToSave) {
        relationshipService.saveRelationship(relationshipToSave);
        sheepService.saveSheep(sheepToSave);
    }

    /**
     * Attempts to breed the two sheep given by their id's and returns their
     * random, unpersisted child. Throws an IllegalArgumentException if the
     * parents cannot be in a relationship or cannot be bred.
     *
     * @param sheep1Id - id of one of the parents
     * @param sheep2Id - id of the other parent
     * @return an unpersisted child Sheep of the two parents.
     */
    @Transactional
    public Sheep breedAndInferSheep(Integer sheep1Id, Integer sheep2Id) {
        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);;

        // create a new child from the two sheep
        Sheep newChild = breedNewSheep(relationship);

        // get the new joint distribution from the additional offspring data
        inferenceEngine.findJointDistribution(relationship); // categorized for ensemble and loopy

        // update the marginal distributions of the parents' using the joint distribution
        inferenceEngine.updateMarginalProbabilities(relationship); // categorized for loopy

        // infer child hidden distribution
        inferenceEngine.inferChildHiddenDistribution(relationship,  newChild);

        for (Category category : Category.values()) {
            newChild.setDistribution(category, DistributionType.INFERRED, newChild.getDistribution(category, DistributionType.PRIOR));
        }

        return newChild;
    }

    /**
     * Breed a new child sheep from the given Relationship. The child randomly takes
     * one allele from each parent and then randomly chooses which one is the phenotype.
     * The child will have default distributions and sets its parents to this relationship.
     * The parents must have known hidden alleles in all categories to breed. Throws
     * an IllegalArgumentException otherwise.
     *
     * @param relationship - the relationship of the two parents to breed
     * @return a Sheep that represents the child born from the relationship
     */
    public static Sheep breedNewSheep(Relationship relationship) {
        breedingValidation(relationship);

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

    // validates that these two sheep can be automatically bred within the app
    private static void breedingValidation(Relationship relationship) {
        Set<Category> parent1Missing = missingHiddenAllele(relationship.getParent1());
        Set<Category> parent2Missing = missingHiddenAllele(relationship.getParent2());
        if (!parent1Missing.isEmpty() || !parent2Missing.isEmpty()) {
            String parent1MissingStr = parent1Missing.isEmpty() ? "" : String.format("Parent 1 is missing hidden alleles in: %s%n", parent1Missing);
            String parent2MissingStr = parent2Missing.isEmpty() ? "" : String.format("Parent 2 is missing hidden alleles in: %s%n", parent2Missing);
            throw new IllegalArgumentException(String.format("Cannot breed sheep with missing hidden alleles!%n%s%s", parent1MissingStr, parent2MissingStr));
        }
    }

    private static Set<Category> missingHiddenAllele(Sheep sheep) {
        Set<Category> categories = EnumSet.noneOf(Category.class);
        for (Category category : Category.values()) {
            if (sheep.getHiddenAllele(category) == null) {
                categories.add(category);
            }
        }
        return categories;
    }
}
