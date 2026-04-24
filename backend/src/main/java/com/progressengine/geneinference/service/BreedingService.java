package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.exception.BadRequestException;
import com.progressengine.geneinference.exception.IncompleteGenotypeException;
import com.progressengine.geneinference.exception.ResourceNotFoundException;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.*;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.GradeAlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;

import com.progressengine.geneinference.repository.BirthRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BreedingService {

    private static final double CERTAINTY_THRESHOLD = 0.99;
    private static final int MAX_SHEEP_PER_CATEGORY = 2;

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    private final BirthRecordRepository birthRecordRepository;

    public BreedingService(SheepService sheepService, RelationshipService relationshipService, BirthRecordRepository birthRecordRepository) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
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
    public BirthRecord breedAndInferSheep(UUID userId, Integer sheep1Id, Integer sheep2Id, boolean saveChild, String name) {
        if (sheep1Id.equals(sheep2Id)) {
            throw new BadRequestException("Parent sheep IDs must be different");
        }

        // find/create the relationship of these two sheep
        Sheep sheep1 = sheepService.findByIdAndUserId(sheep1Id, userId);
        Sheep sheep2 = sheepService.findByIdAndUserId(sheep2Id, userId);
        Relationship relationship = relationshipService.findOrCreateRelationship(userId, sheep1, sheep2);

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
            newChild.setUserId(userId);
            birthRecord = relationship.addChildToRelationship(sheepService.saveSheep(newChild));
        } else {
            birthRecord = relationship.addChildInformationToRelationship(newChild);
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
        Random random = new Random();

        for (Category category : Category.values()) {
            selectAllelesForChildCategory(child, sheep1, sheep2, category, random);
        }

        child.syncPriorsFromObservedPhenotypes();
        child.copyAllPriorsToInferred();
        return child;
    }

    private static <A extends Enum<A> & Allele> void selectAllelesForChildCategory(Sheep child, Sheep p1, Sheep p2, Category category, Random random) {
        if (p1.getHiddenAllele(category) == null || p2.getHiddenAllele(category) == null) {
            throw new IllegalArgumentException("Missing known hidden allele in category " + category);
        }
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A parent1Allele = random.nextBoolean() // assumes no inheritance bias
            ? p1.getPhenotype(category)
            : p1.getHiddenAllele(category);

        A parent2Allele = random.nextBoolean()
            ? p2.getPhenotype(category)
            : p2.getHiddenAllele(category);

        AllelePair<A> expressedOrder = domain.sampleExpressionOrder(parent1Allele, parent2Allele, random);

        child.setGenotype(category, expressedOrder);
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
    public BirthRecord createAndInferSheep(UUID userId, SheepBreedRequestDTO childRequest, boolean saveChild) {
        Sheep child = DomainMapper.fromRequestDTO(childRequest);
        child.setUserId(userId);

        Sheep parent1 = sheepService.findByIdAndUserId(childRequest.getParent1Id(), userId);
        Sheep parent2 = sheepService.findByIdAndUserId(childRequest.getParent2Id(), userId);
        Relationship relationship = relationshipService.findOrCreateRelationship(userId, parent1, parent2);
        /* ----------------------------------------------------------------------------------------
        * possibly belongs in relationship domain for invariant control */
        BirthRecord birthRecord;
        if (saveChild) {
            birthRecord = relationship.addChildToRelationship(sheepService.saveSheep(child));
        } else {
            birthRecord = relationship.addChildInformationToRelationship(child);
        }

        return birthRecord;
    }

    public BirthRecord findBirthRecordById(Integer id) {
        return birthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birth record not found"));
    }

    public PredictionResponseDTO predictChild(UUID userId, Integer sheep1Id, Integer sheep2Id) { // Update prediction to hold generic categories
        Sheep sheep1 = sheepService.findByIdAndUserId(sheep1Id, userId);
        Sheep sheep2 = sheepService.findByIdAndUserId(sheep2Id, userId);

        return new PredictionResponseDTO(InferenceMath.predictChildrenDistributions(sheep1, sheep2));
    }

    public List<BestPredictionDTO> bestPredictions(UUID userId) { // update to only base best off grades but include all categories for prediction
        List<Sheep> allSheep = sheepService.getAllSheep(userId);
        if (allSheep.size() < 2) {
            throw new IllegalStateException(
                    "At least two sheep are required to compute best predictions"
            );
        }

        // loop through all sheep and keep track of the sheep that are the best in a category
        Map<Category, PriorityQueue<Sheep>> bestSheepMap = new EnumMap<>(Category.class);
        for (Sheep sheep : allSheep) {
            for (Category category : gradeCategories()) {
                PriorityQueue<Sheep> bestSheepQueue = bestSheepMap.computeIfAbsent(category,
                        k -> new PriorityQueue<>((a, b) -> compareGradeCategory(a, b, category)));
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
                Map<Category, Map<String, Double>> predictionMap = InferenceMath.predictChildrenDistributions(parent1, parent2);
                SheepSummaryResponseDTO parent1Summary = new SheepSummaryResponseDTO(parent1.getId(), parent1.getName());
                SheepSummaryResponseDTO parent2Summary = new SheepSummaryResponseDTO(parent2.getId(), parent2.getName());
                predictions.add(new BestPredictionDTO(parent1Summary, parent2Summary, sheepToCategoryMap.get(parent1), sheepToCategoryMap.get(parent2), predictionMap));
            }
        }
        predictions.sort(null);

        return predictions;
    }

    @Transactional
    public List<Map<Category, Map<String, Double>>> recalculateAll(UUID userId) {
        List<Sheep> allSheep = sheepService.getAllSheep(userId);
        List<Relationship> allRelationship = relationshipService.getAllRelationships(userId);

        FactorGraph factorGraph = new FactorGraph(allSheep, allRelationship);
        factorGraph.recalculateAllMessages();
        List<Map<Category, Map<String, Double>>> newBeliefs = factorGraph.computeBeliefs();

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

    private List<Category> gradeCategories() {
        return Arrays.stream(Category.values())
                .filter(category -> CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)
                .toList();
    }

    private int compareGradeCategory(Sheep sheep1, Sheep sheep2, Category category) {
        if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)) {
            throw new IllegalArgumentException("Tried to compare Grade categories on a non-grade category");
        }
 
        Grade sheep1Best = bestGradeInCategory(sheep1, category);
        Map<Grade, Double> sheep1Dist = sheep1.getDistribution(category, DistributionType.INFERRED);
        double sheep1Entropy = InferenceMath.entropy(sheep1Dist);

        Grade sheep2Best = bestGradeInCategory(sheep2, category);
        Map<Grade, Double> sheep2Dist = sheep2.getDistribution(category, DistributionType.INFERRED);
        double sheep2Entropy = InferenceMath.entropy(sheep2Dist);

        if (sheep1Best.isBetterThan(sheep2Best)) {
            return 1;
        } else if (sheep2Best.isBetterThan(sheep1Best)) {
            return -1;
        }
        return -Double.compare(sheep1Entropy, sheep2Entropy);
    }

    private Grade bestGradeInCategory(Sheep sheep, Category category) {
        if (!(CategoryDomains.domainFor(category) instanceof GradeAlleleDomain)) {
            throw new IllegalArgumentException("Tried to find best grade in a non-grade category");
        }

        Grade bestGrade = sheep.getPhenotype(category);
        Map<Grade, Double> gradeDist = sheep.getDistribution(category, DistributionType.INFERRED);
        for (Map.Entry<Grade, Double> entry : gradeDist.entrySet()) {
            if (entry.getValue() >= CERTAINTY_THRESHOLD && entry.getKey().isBetterThan(bestGrade)) {
                bestGrade = entry.getKey();
            }
        }
        return bestGrade;
    }

}
