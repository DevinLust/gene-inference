package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.service.BreedingService;
import com.progressengine.geneinference.service.SheepService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(value = "/breed")
public class BreedController {
    private final SheepService sheepService;
    private  final BreedingService breedingService;

    public BreedController(SheepService sheepService, BreedingService breedingService) {
        this.sheepService = sheepService;
        this.breedingService = breedingService;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public ResponseEntity<?> breed(@Positive @PathVariable Integer sheep1Id, @Positive @PathVariable Integer sheep2Id, @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild) {
        Sheep newChild = breedingService.breedAndInferSheep(sheep1Id, sheep2Id, saveChild);

        return ResponseEntity.ok(sheepService.toResponseDTO(newChild));
    }

    @GetMapping("/{sheep1Id}/{sheep2Id}/predict")
    public ResponseEntity<?> predictBreeding(@Positive @PathVariable Integer sheep1Id, @Positive @PathVariable Integer sheep2Id) {
        return ResponseEntity.ok(breedingService.predictChild(sheep1Id, sheep2Id));
    }

    @GetMapping("/best-predictions")
    public ResponseEntity<?> getBestPredictions() {
        return ResponseEntity.ok(breedingService.bestPredictions());
    }

    @GetMapping("/recalculate-beliefs")
    public ResponseEntity<?> getRecalculateBeliefs() {
        return ResponseEntity.ok(breedingService.recalculateAll());
    }

}
