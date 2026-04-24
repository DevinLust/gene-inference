package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.DistributionType;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.SheepService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<?> addSheep(
            @Valid @RequestBody SheepNewRequestDTO sheepNewRequestDTO,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        Sheep child = sheepService.saveNewSheep(sheepNewRequestDTO, userId);

        return ResponseEntity.ok().body(DomainMapper.toResponseDTO(child));
    }

    @GetMapping
    public List<SheepSummaryResponseDTO> filterSheep(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Grade> grades,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        // convert list to set to remove duplicates
        Set<Grade> gradeSet = grades == null ? Collections.emptySet() : new HashSet<>(grades);

        return sheepService.filterSheepByNameAndGrade(userId, name, gradeSet);
    }

    @GetMapping("/distributions")
    public DistributionResponseDTO distributions(
            @RequestParam Category category,
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(defaultValue = "INFERRED") DistributionType type,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return sheepService.getDistributionProjectionsByCategoryAndType(userId, category, type, ids);
    }

    @GetMapping("/{sheepId:\\d+}")
    public SheepResponseDTO getSheep(@Positive @PathVariable Integer sheepId,
                                     @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return sheepService.getSheepResponseDTO(sheepId, userId);
    }

    @GetMapping("/{sheepId}/parents")
    public ResponseEntity<?> getParents(@Positive @PathVariable Integer sheepId,
                                        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(sheepService.getParents(userId, sheepId));
    }

    @GetMapping("/{sheepId}/children")
    public ResponseEntity<?> getChildren(@Positive @PathVariable Integer sheepId,
                                         @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(sheepService.getChildren(userId, sheepId));
    }

    @GetMapping("/{sheepId}/partners")
    public ResponseEntity<?> getPartners(@Positive @PathVariable Integer sheepId,
                                         @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(sheepService.getPartners(userId, sheepId));
    }

    @PostMapping("/{id}/evolve/{category}")
    public ResponseEntity<?> evolve(@PathVariable Integer id,
                                    @PathVariable Category category,
                                    @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(sheepService.evolvePhenotype(userId, id, category));
    }

//    @Deprecated
//    @PutMapping("/{sheepId}")
//    public ResponseEntity<?> replaceSheep(@Positive @PathVariable Integer sheepId, @Valid @RequestBody SheepReplaceRequestDTO replacementSheep) {
//        Sheep updatedSheep = sheepService.replaceSheep(sheepId, replacementSheep);
//        return ResponseEntity.ok(DomainMapper.toResponseDTO(updatedSheep));
//    }

    @PatchMapping("/{sheepId}")
    public ResponseEntity<?> updateSheep(@Positive @PathVariable Integer sheepId,
                                         @Valid @RequestBody SheepUpdateRequestDTO updateSheepModel,
                                         @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Sheep updatedSheep = sheepService.updateSheep(userId, sheepId, updateSheepModel);
        return ResponseEntity.ok(DomainMapper.toResponseDTO(updatedSheep));
    }

    @DeleteMapping("/{sheepId}")
    public ResponseEntity<?> deleteSheep(@Positive @PathVariable Integer sheepId,
                                         @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        sheepService.deleteSheep(userId, sheepId);
        return ResponseEntity.noContent().build();
    }

}
