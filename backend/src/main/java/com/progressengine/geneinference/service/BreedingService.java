package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.BestPredictionDTO;
import com.progressengine.geneinference.dto.PredictionResponseDTO;
import com.progressengine.geneinference.dto.SheepNewRequestDTO;
import com.progressengine.geneinference.exception.BadRequestException;
import com.progressengine.geneinference.exception.IncompleteGenotypeException;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BreedingService {

    private static final double CERTAINTY_THRESHOLD = 0.99;
    private static final int MAX_SHEEP_PER_CATEGORY = 2;

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final InferenceEngine inferenceEngine;

    public BreedingService(SheepService sheepService, RelationshipService relationshipService, @Qualifier("loopy") InferenceEngine inferenceEngine) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.inferenceEngine = inferenceEngine;
    }


    /**
     * Breeds the two sheep identified by the given IDs and returns their randomly
     * generated child. If {@code saveChild} is {@code true}, the child is also
     * persisted.
     *
     * @param sheep1Id
     *     the ID of the first parent sheep
     * @param sheep2Id
     *     the ID of the second parent sheep
     * @param saveChild
     *     if {@code true}, persists the newly created child sheep;
     *     if {@code false}, returns the child without saving it
     * @return
     *     the child sheep produced by breeding the two parents
     * @throws {@code ResourceNotFoundException}
     *     if either parent sheep does not exist
     * @throws {@code IllegalArgumentException}
     *     if the sheep ids refer to the same sheep
     */
    @Transactional
    public Sheep breedAndInferSheep(Integer sheep1Id, Integer sheep2Id, boolean saveChild) {
        Sheep newChild = breedAndInferSheep(sheep1Id, sheep2Id);

        // save new child
        if (saveChild) {
            sheepService.saveSheep(newChild);
        }
        return newChild;
    }

    /**
     * Breed the two sheep given by their id's and returns their
     * random, unpersisted child.
     *
     * @param sheep1Id
     *     the ID of the first parent sheep
     * @param sheep2Id
     *     the ID of the second parent sheep
     * @return
     *     the child sheep produced by breeding the two parents
     * @throws {@code ResourceNotFoundException}
     *     if either parent sheep does not exist
     * @throws {@code BadRequestException}
     *     if the sheep ids refer to the same sheep
     */
    @Transactional
    public Sheep breedAndInferSheep(Integer sheep1Id, Integer sheep2Id) {
        if (sheep1Id.equals(sheep2Id)) {
            throw new BadRequestException("Parent sheep IDs must be different");
        }

        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);

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

        // TODO - propagate probability to other partners and children

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


    /**
     * Create a new sheep from the given request. If the child
     * comes from explicit parents it will assign the child and
     * infer new distributions for everyone.
     *
     * @param childRequest
     *     the Sheep request to be assigned as the new child
     * @return
     *     the child sheep produced by breeding the two parents
     * @throws {@code ResourceNotFoundException}
     *     if either parent sheep does not exist
     */
    @Transactional
    public Sheep createAndInferSheep(SheepNewRequestDTO childRequest) {
        Sheep child = sheepService.fromRequestDTO(childRequest);

        Relationship relationship = child.getParentRelationship();
        // check if there is nothing to assign or infer
        if (relationship == null) {
            return sheepService.saveSheep(child);
        }


        // check to make sure it logically makes sense for this to be a child of the relationship
        // TODO - update the method to check that it will work
        assignSheep(relationship, child);

        // get the new joint distribution from the additional offspring data
        inferenceEngine.findJointDistribution(relationship); // categorized for ensemble and loopy

        // update the marginal distributions of the parents' using the joint distribution
        inferenceEngine.updateMarginalProbabilities(relationship); // categorized for loopy

        // infer child hidden distribution
        inferenceEngine.inferChildHiddenDistribution(relationship,  child);

        for (Category category : Category.values()) {
            child.setDistribution(category, DistributionType.INFERRED, child.getDistribution(category, DistributionType.PRIOR));
        }

        return sheepService.saveSheep(child);
    }

    private void assignSheep(Relationship relationship, Sheep child) {
        // check to make sure it logically makes sense for this to be a child of the relationship
        // TODO - add business logic check here

        // update phenotype frequency
        for (Category category : Category.values()) {
            relationship.updatePhenotypeFrequency(category, child.getPhenotype(category), 1);
        }
    }

    public PredictionResponseDTO predictChild(Integer sheep1Id, Integer sheep2Id) {
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);

        return new PredictionResponseDTO(inferenceEngine.predictChildrenDistributions(sheep1, sheep2));
    }

    public List<BestPredictionDTO> bestPredictions() {
        List<Sheep> allSheep = sheepService.getAllSheep();
        if (allSheep.size() < 2) {
            throw new IllegalStateException(
                    "At least two sheep are required to compute best predictions"
            );
        }

        // loop through all sheep and keep track of the sheep that are the best in a category
        Map<Category, PriorityQueue<Sheep>> bestSheepMap = new EnumMap<>(Category.class);
        for (Sheep sheep : allSheep) {
            for (Category category : Category.values()) {
                PriorityQueue<Sheep> bestSheepQueue = bestSheepMap.computeIfAbsent(category,
                        k -> new PriorityQueue<>((a, b) -> compareCategory(a, b, category)));
                bestSheepQueue.add(sheep);
                if (bestSheepQueue.size() > MAX_SHEEP_PER_CATEGORY) {
                    bestSheepQueue.poll();
                }
            }
        }

        // gather the best sheep in a Map and make a prediction for all pairs
        Map<Sheep, Map<Category, Grade>> sheepToCategoryMap = new HashMap<>();
        for (Map.Entry<Category, PriorityQueue<Sheep>> entry : bestSheepMap.entrySet()) {
            while (!entry.getValue().isEmpty()) {
                Sheep sheep = entry.getValue().poll();
                Category category = entry.getKey();
                Map<Category, Grade>  categoryToGradeMap = sheepToCategoryMap.computeIfAbsent(sheep, k -> new EnumMap<>(Category.class));
                categoryToGradeMap.put(category, bestGradeInCategory(sheep, category));
                sheepToCategoryMap.put(sheep, categoryToGradeMap);
            }
        }

        // pairwise predict all parents
        List<BestPredictionDTO> predictions = new ArrayList<>();
        List<Sheep> bestSheepList = new ArrayList<>(sheepToCategoryMap.keySet());
        for (int i = 0; i < bestSheepList.size() - 1; i++) {
            for (int j = i + 1; j < bestSheepList.size(); j++) {
                Sheep parent1 = bestSheepList.get(i);
                Sheep parent2 = bestSheepList.get(j);
                Map<Category, Map<Grade, Double>> predictionMap = inferenceEngine.predictChildrenDistributions(parent1, parent2);
                predictions.add(new BestPredictionDTO(parent1, parent2, sheepToCategoryMap.get(parent1), sheepToCategoryMap.get(parent2), predictionMap));
            }
        }
        predictions.sort(null);

        return predictions;
    }

    // validates that these two sheep can be automatically bred within the app
    private static void breedingValidation(Relationship relationship) {
        Set<Category> parent1Missing = missingHiddenAllele(relationship.getParent1());
        Set<Category> parent2Missing = missingHiddenAllele(relationship.getParent2());
        if (!parent1Missing.isEmpty() || !parent2Missing.isEmpty()) {
            throw new IncompleteGenotypeException(parent1Missing, parent2Missing);
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

    private int compareCategory(Sheep sheep1, Sheep sheep2, Category category) {
        Grade sheep1Best = bestGradeInCategory(sheep1, category);
        int sheep1Certainty = hiddenDistributionCertainty(sheep1, category);
        Grade sheep2Best = bestGradeInCategory(sheep2, category);
        int sheep2Certainty = hiddenDistributionCertainty(sheep2, category);

        if (sheep1Best.isBetterThan(sheep2Best)) {
            return 1;
        } else if (sheep2Best.isBetterThan(sheep1Best)) {
            return -1;
        }
        return Integer.compare(sheep1Certainty, sheep2Certainty);
    }

    private Grade bestGradeInCategory(Sheep sheep, Category category) {
        Grade bestGrade = sheep.getPhenotype(category);
        for (Map.Entry<Grade, Double> entry : sheep.getDistribution(category, DistributionType.INFERRED).entrySet()) {
            if (entry.getValue() > CERTAINTY_THRESHOLD && entry.getKey().isBetterThan(bestGrade)) {
                bestGrade = entry.getKey();
            }
        }
        return bestGrade;
    }

    private int hiddenDistributionCertainty(Sheep sheep, Category category) {
        int certainty = 0;
        for (Map.Entry<Grade, Double> entry : sheep.getDistribution(category, DistributionType.INFERRED).entrySet()) {
            if (entry.getValue() > CERTAINTY_THRESHOLD) {
                certainty += 5;
            } else if (entry.getValue() == 0.0) { // rewards distributions with true certainty
                certainty++;
            }
        }
        return certainty;
    }

}
