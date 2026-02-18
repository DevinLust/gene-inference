package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.SheepBreedRequestDTO;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.service.BreedingService;
import jakarta.validation.Valid;
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
        BirthRecord birthRecord = breedingService.breedAndInferSheep(sheep1Id, sheep2Id, saveChild);

        return ResponseEntity.ok(DomainMapper.toResponseDTO(birthRecord));
    }

    @PostMapping(value = "/record-birth")
    public ResponseEntity<?> createChild(@Valid @RequestBody SheepBreedRequestDTO sheepBreedRequestDTO, @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild) {
        BirthRecord birthRecord = breedingService.createAndInferSheep(sheepBreedRequestDTO, saveChild);

        return ResponseEntity.ok(DomainMapper.toResponseDTO(birthRecord));
    }

    @GetMapping("/{sheep1Id}/{sheep2Id}/predict")
    public ResponseEntity<?> predictBreeding(@Positive @PathVariable Integer sheep1Id, @Positive @PathVariable Integer sheep2Id) {
        return ResponseEntity.ok(breedingService.predictChild(sheep1Id, sheep2Id));
    }

    @GetMapping("/best-predictions")
    public ResponseEntity<?> getBestPredictions() {
        return ResponseEntity.ok(breedingService.bestPredictions());
    }

    @PostMapping("/recalculate-beliefs")
    public ResponseEntity<?> recalculateBeliefs() {
        return ResponseEntity.ok(breedingService.recalculateAll());
    }

}
