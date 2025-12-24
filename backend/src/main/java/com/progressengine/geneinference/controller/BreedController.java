package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.BestPredictionDTO;
import com.progressengine.geneinference.dto.PredictionResponseDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.BreedingService;
import com.progressengine.geneinference.service.InferenceEngine;
import com.progressengine.geneinference.service.RelationshipService;
import com.progressengine.geneinference.service.SheepService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/breed")
public class BreedController {

    private static final double CERTAINTY_THRESHOLD = 0.99;
    private static final int MAX_SHEEP_PER_CATEGORY = 2;

    private final SheepService sheepService;
    private final RelationshipService relationshipService;
    public final BreedingService breedingService;
    private final InferenceEngine inferenceEngine;

    public BreedController(SheepService sheepService, RelationshipService relationshipService, BreedingService breedingService, @Qualifier("loopy") InferenceEngine inferenceEngine) {
        this.sheepService = sheepService;
        this.relationshipService = relationshipService;
        this.breedingService = breedingService;
        this.inferenceEngine = inferenceEngine;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public ResponseEntity<?> breed(@PathVariable Integer sheep1Id, @PathVariable Integer sheep2Id, @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild) {
        // find/create the relationship of these two sheep
        Sheep newChild;
        try {
            newChild = breedingService.breedAndInferSheep(sheep1Id, sheep2Id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // save new child
        sheepService.saveSheep(newChild);

        // TODO - propagate probability to other partners and children

        StringBuilder childResults = new StringBuilder();
        childResults.append("Relationship ID: ").append(newChild.getParentRelationship().getId()).append("\n");

        newChild.getGenotypes().forEach((category, genotype) ->
                childResults.append(category).append(": ").append(genotype.getPhenotype()).append("\n")
        );

        return ResponseEntity.ok(childResults.toString());
    }

    @GetMapping("/{sheep1Id}/{sheep2Id}/predict")
    public ResponseEntity<?> predictBreeding(@PathVariable Integer sheep1Id, @PathVariable Integer sheep2Id) {
        Sheep sheep1 = sheepService.findById(sheep1Id);
        Sheep sheep2 = sheepService.findById(sheep2Id);

        PredictionResponseDTO prediction = new PredictionResponseDTO(inferenceEngine.predictChildrenDistributions(sheep1, sheep2));

        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/best-predictions")
    public ResponseEntity<?> getBestPredictions() {
        List<Sheep> allSheep = sheepService.getAllSheep();
        if (allSheep.isEmpty() || allSheep.size() < 2) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Not enough sheep to make a prediction.");
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

        return ResponseEntity.ok(predictions);
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
