package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.SheepBreedRequestDTO;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.service.BreedingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Validated
@RequestMapping(value = "/breed")
public class BreedController {
    private  final BreedingService breedingService;

    public BreedController(BreedingService breedingService) {
        this.breedingService = breedingService;
    }

    @PostMapping(value = "/{sheep1Id}/{sheep2Id}")
    public ResponseEntity<?> breed(@Positive @PathVariable Integer sheep1Id,
                                   @Positive @PathVariable Integer sheep2Id,
                                   @RequestParam(name = "saveChild", defaultValue = "true") boolean saveChild,
                                   @RequestParam(name = "name", required = false) String name,
                                   @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        BirthRecord birthRecord = breedingService.breedAndInferSheep(userId, sheep1Id, sheep2Id, saveChild, name);

        return ResponseEntity.ok(DomainMapper.toResponseDTO(birthRecord));
    }

    @PostMapping(value = "/record-birth")
    public ResponseEntity<?> createChild(@Valid @RequestBody SheepBreedRequestDTO sheepBreedRequestDTO,
                                         @RequestParam(defaultValue = "true") boolean saveChild,
                                         @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        BirthRecord birthRecord = breedingService.createAndInferSheep(userId, sheepBreedRequestDTO, saveChild);

        return ResponseEntity.ok(DomainMapper.toResponseDTO(birthRecord));
    }

    @GetMapping("/{sheep1Id}/{sheep2Id}/predict")
    public ResponseEntity<?> predictBreeding(@Positive @PathVariable Integer sheep1Id,
                                             @Positive @PathVariable Integer sheep2Id,
                                             @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(breedingService.predictChild(userId, sheep1Id, sheep2Id));
    }

    @GetMapping("/best-predictions")
    public ResponseEntity<?> getBestPredictions(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(breedingService.bestPredictions(userId));
    }

    @PostMapping("/recalculate-beliefs")
    public ResponseEntity<?> recalculateBeliefs(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(breedingService.recalculateAll(userId));
    }

}
