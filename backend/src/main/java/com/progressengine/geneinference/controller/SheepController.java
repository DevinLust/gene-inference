package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Validated
@RequestMapping(value = "/sheep")
public class SheepController {

    private final SheepService sheepService;

    public SheepController(SheepService sheepService) {
        this.sheepService = sheepService;
    }

    @PostMapping(consumes = {"application/json", "application/json;charset=UTF-8"})
    public ResponseEntity<?> addSheep(@Valid @RequestBody SheepNewRequestDTO sheepNewRequestDTO) {
        return ResponseEntity.ok().body(sheepService.saveNewSheep(sheepNewRequestDTO));
    }

    @GetMapping
    public List<SheepResponseDTO> getAllSheep() {
        List<Sheep> sheepList = sheepService.getAllSheep();
        return sheepList.stream()
                .map(sheepService::toResponseDTO)
                .toList();
    }

    @GetMapping("/filter")
    public List<SheepResponseDTO> filterSheep(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Grade> grades) {

        // convert list to set to remove duplicates
        Set<Grade> gradeSet = grades == null ? Collections.emptySet() : new HashSet<>(grades);

        return sheepService.filterSheep(name, gradeSet).stream()
                .map(sheepService::toResponseDTO)
                .toList();
    }

    @GetMapping("/{sheepId}")
    public SheepResponseDTO getSheep(@Positive @PathVariable Integer sheepId) {
        Sheep sheep = sheepService.findById(sheepId);
        return sheepService.toResponseDTO(sheep);
    }

    @GetMapping("/{sheepId}/parents")
    public ResponseEntity<?> getParents(@Positive @PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getParents(sheepId));
    }

    @GetMapping("/{sheepId}/children")
    public ResponseEntity<?> getChildren(@Positive @PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getChildren(sheepId));
    }

    @GetMapping("/{sheepId}/partners")
    public ResponseEntity<?> getPartners(@Positive @PathVariable Integer sheepId) {
        return ResponseEntity.ok(sheepService.getPartners(sheepId));
    }

    @PutMapping("/{sheepId}")
    public ResponseEntity<?> replaceSheep(@Positive @PathVariable Integer sheepId, @Valid @RequestBody SheepReplaceRequestDTO replacementSheep) {
        Sheep updatedSheep = sheepService.replaceSheep(sheepId, replacementSheep);
        return ResponseEntity.ok(sheepService.toResponseDTO(updatedSheep));
    }

    @PatchMapping("/{sheepId}")
    public ResponseEntity<?> updateSheep(@Positive @PathVariable Integer sheepId, @Valid @RequestBody SheepUpdateRequestDTO updateSheepModel) {
        Sheep updatedSheep = sheepService.updateSheep(sheepId, updateSheepModel);
        return ResponseEntity.ok(sheepService.toResponseDTO(updatedSheep));
    }

    @DeleteMapping("/{sheepId}")
    public ResponseEntity<?> deleteSheep(@Positive @PathVariable Integer sheepId) {
        sheepService.deleteSheep(sheepId);
        return ResponseEntity.noContent().build();
    }

}
