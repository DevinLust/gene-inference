package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/sheep")
public class SheepController {

    private final SheepService sheepService;

    public SheepController(SheepService sheepService) {
        this.sheepService = sheepService;
    }

    @Transactional
    @PostMapping(consumes = {"application/json", "application/json;charset=UTF-8"})
    public ResponseEntity<?> addSheep(@RequestBody SheepNewRequestDTO sheepNewRequestDTO) {
        Sheep sheep;
        try {
            sheep = sheepService.fromRequestDTO(sheepNewRequestDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(sheepService.saveSheep(sheep));
    }

    @GetMapping
    public List<SheepResponseDTO> getAllSheep() {
        List<Sheep> sheepList = sheepService.getAllSheep();
        return sheepList.stream()
                .map(sheepService::toResponseDTO)
                .toList();
    }

    @GetMapping("/{sheepId}")
    public SheepResponseDTO getSheep(@PathVariable Integer sheepId) {
        Sheep sheep = sheepService.getSheepById(sheepId);
        return sheepService.toResponseDTO(sheep);
    }

    @GetMapping("/{sheepId}/parents")
    public ResponseEntity<?> getParents(@PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getParents(sheepId));
    }

    @GetMapping("/{sheepId}/children")
    public ResponseEntity<?> getChildren(@PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getChildren(sheepId));
    }

    @GetMapping("/{sheepId}/partners")
    public ResponseEntity<?> getPartners(@PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getPartners(sheepId));
    }

    @Transactional
    @PutMapping("/{sheepId}")
    public ResponseEntity<?> replaceSheep(@PathVariable Integer sheepId, @RequestBody SheepReplaceRequestDTO replacementSheep) {
        Sheep updatedSheep = sheepService.fromRequestDTO(replacementSheep);
        updatedSheep.setId(sheepId);
        return ResponseEntity.ok(sheepService.saveSheep(updatedSheep));
    }

    @Transactional
    @PatchMapping("/{sheepId}")
    public ResponseEntity<?> updateSheep(@PathVariable Integer sheepId, @RequestBody SheepUpdateRequestDTO updateSheepModel) {
        Sheep sheep = sheepService.findById(sheepId);

        if (updateSheepModel.getName() != null) {
            sheep.setName(updateSheepModel.getName());
        }

        Map<Category, SheepGenotypeDTO> updatedGenotypes = updateSheepModel.getGenotypes();
        if (updatedGenotypes != null) {
            for (Map.Entry<Category, SheepGenotypeDTO> entry : updatedGenotypes.entrySet()) {
                GradePair genotype = entry.getValue().toGradePair();
                sheep.setGenotype(entry.getKey(), genotype);
            }
        }

        Map<Category, Map<Grade, Double>> updatedPriors = updateSheepModel.getDistributions();
        if (updatedPriors != null) {
            for (Map.Entry<Category, Map<Grade, Double>> entry : updatedPriors.entrySet()) {
                sheep.setDistribution(entry.getKey(), DistributionType.PRIOR, entry.getValue());
            }
        }

        return ResponseEntity.ok(sheepService.saveSheep(sheep));
    }

    @Transactional
    @DeleteMapping("/{sheepId}")
    public ResponseEntity<?> deleteSheep(@PathVariable Integer sheepId) {
        sheepService.deleteSheep(sheepId);
        return ResponseEntity.noContent().build();
    }

}
