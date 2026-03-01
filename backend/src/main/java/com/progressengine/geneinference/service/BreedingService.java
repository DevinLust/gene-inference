package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.exception.BadRequestException;
import com.progressengine.geneinference.exception.IncompleteGenotypeException;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;

import com.progressengine.geneinference.repository.BirthRecordRepository;
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
    private final BirthRecordRepository birthRecordRepository;

    public BreedingService(SheepService sheepService, RelationshipService relationshipService, @Qualifier("loopy") InferenceEngine inferenceEngine, BirthRecordRepository birthRecordRepository) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.inferenceEngine = inferenceEngine;
        this.birthRecordRepository = birthRecordRepository;
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
    public BirthRecord breedAndInferSheep(Integer sheep1Id, Integer sheep2Id, boolean saveChild, String name) {
        if (sheep1Id.equals(sheep2Id)) {
            throw new BadRequestException("Parent sheep IDs must be different");
        }

        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);
        Relationship relationship = relationshipService.findOrCreateRelationship(sheep1, sheep2);

        /* ----------------------------------------------------------------------------------------
         * possibly belongs in relationship domain for invariant control */
        // create a new child from the two sheep
        Sheep newChild = breedNewSheep(sheep1, sheep2);
        if (name != null && saveChild) {
            newChild.setName(name);
        }

        // save new child if told and create a BirthRecord for the event
        BirthRecord birthRecord;
        if (saveChild) {
            birthRecord = relationship.addChildToRelationship(sheepService.saveSheep(newChild));
        } else {
            birthRecord = relationship.addChildInformationToRelationship(newChild);
        }

        for (Category category : Category.values()) {
            newChild.setDistribution(category, DistributionType.INFERRED, newChild.getDistribution(category, DistributionType.PRIOR));
        }

        return birthRecord;
    }


    /**
     * Breed a new child sheep from two sheep. The child randomly takes
     * one allele from each parent and then randomly chooses which one is the phenotype.
     * The child will have default distributions.
     * The parents must have known hidden alleles in all categories to breed. Throws
     * an IncompleteGenotypeException otherwise.
     *
     * @param sheep1 - the first sheep to breed
     * @param sheep2 - the second sheep to breed
     * @return a Sheep that represents the child born from the relationship
     */
    public static Sheep breedNewSheep(Sheep sheep1, Sheep sheep2) {
        breedingValidation(sheep1, sheep2);

        Sheep child = new Sheep();

        // random genotype from the two parents, one allele from each parent
        Random random = new Random();
        // -----------------------------------------------------------------------------------
        for (Category category : Category.values()) {
            if (sheep1.getHiddenAllele(category) == null || sheep2.getHiddenAllele(category) == null) {
                throw new IllegalArgumentException("Missing known hidden allele in category " + category);
            }
            Grade newPhenotype;
            Grade newHiddenAllele;
            if (random.nextBoolean()) {
                newPhenotype = random.nextBoolean() ? sheep1.getPhenotype(category) : sheep1.getHiddenAllele(category);
                newHiddenAllele = random.nextBoolean() ? sheep2.getPhenotype(category) : sheep2.getHiddenAllele(category);
            } else {
                newPhenotype = random.nextBoolean() ? sheep2.getPhenotype(category) : sheep2.getHiddenAllele(category);
                newHiddenAllele = random.nextBoolean() ? sheep1.getPhenotype(category) : sheep1.getHiddenAllele(category);
            }

            child.setPhenotype(category, newPhenotype);
            child.setHiddenAllele(category, newHiddenAllele);
        }
        //relationship.addChildToRelationship(child);
        child.createDefaultDistributions();
        // ---------------------------------------------------------------------------------

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
    public BirthRecord createAndInferSheep(SheepBreedRequestDTO childRequest, boolean saveChild) {
        Sheep child = DomainMapper.fromRequestDTO(childRequest);

        Sheep parent1 = sheepService.findById(childRequest.getParent1Id());
        Sheep parent2 = sheepService.findById(childRequest.getParent2Id());
        Relationship relationship = relationshipService.findOrCreateRelationship(parent1, parent2);
        /* ----------------------------------------------------------------------------------------
        * possibly belongs in relationship domain for invariant control */
        BirthRecord birthRecord;
        if (saveChild) {
            birthRecord = relationship.addChildToRelationship(sheepService.saveSheep(child));
        } else {
            birthRecord = relationship.addChildInformationToRelationship(child);
        }

        for (Category category : Category.values()) {
            child.setDistribution(category, DistributionType.INFERRED, child.getDistribution(category, DistributionType.PRIOR));
        }

        return birthRecord;
    }

    public BirthRecord findBirthRecordById(Integer id) {
        return birthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birth record not found"));
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
                SheepSummaryResponseDTO parent1Summary = new SheepSummaryResponseDTO(parent1.getId(), parent1.getName());
                SheepSummaryResponseDTO parent2Summary = new SheepSummaryResponseDTO(parent2.getId(), parent2.getName());
                predictions.add(new BestPredictionDTO(parent1Summary, parent2Summary, sheepToCategoryMap.get(parent1), sheepToCategoryMap.get(parent2), predictionMap));
            }
        }
        predictions.sort(null);

        return predictions;
    }

    @Transactional
    public List<Map<Category, Map<Grade, Double>>> recalculateAll() {
        List<Sheep> allSheep = sheepService.getAllSheep();
        List<Relationship> allRelationship = relationshipService.getAllRelationships();

        FactorGraph factorGraph = new FactorGraph(allSheep, allRelationship);
        factorGraph.recalculateAllMessages();
        List<Map<Category, Map<Grade, Double>>> newBeliefs = factorGraph.computeBeliefs();

        return newBeliefs;
    }

    // validates that these two sheep can be automatically bred within the app
    private static void breedingValidation(Sheep parent1, Sheep parent2) {
        Set<Category> parent1Missing = missingHiddenAllele(parent1);
        Set<Category> parent2Missing = missingHiddenAllele(parent2);
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
