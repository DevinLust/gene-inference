package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.service.BreedingService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(value = "/breed")
public class BreedController {
    private  final BreedingService breedingService;

    public BreedController(BreedingService breedingService) {
        this.breedingService = breedingService;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public ResponseEntity<?> breed(@Positive @PathVariable Integer sheep1Id, @Positive @PathVariable Integer sheep2Id, @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild) {
        Sheep newChild = breedingService.breedAndInferSheep(sheep1Id, sheep2Id, saveChild);

        StringBuilder childResults = new StringBuilder();
        childResults.append("Relationship ID: ").append(newChild.getParentRelationship().getId()).append("\n");

        newChild.getGenotypes().forEach((category, genotype) ->
                childResults.append(category).append(": ").append(genotype.phenotype()).append("\n")
        );

        return ResponseEntity.ok(childResults.toString());
    }

    @GetMapping("/{sheep1Id}/{sheep2Id}/predict")
    public ResponseEntity<?> predictBreeding(@Positive @PathVariable Integer sheep1Id, @Positive @PathVariable Integer sheep2Id) {
        return ResponseEntity.ok(breedingService.predictChild(sheep1Id, sheep2Id));
    }

    @GetMapping("/best-predictions")
    public ResponseEntity<?> getBestPredictions() {
        return ResponseEntity.ok(breedingService.bestPredictions());
    }

}
