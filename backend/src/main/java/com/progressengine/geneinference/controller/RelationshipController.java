package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.BirthRecordDTO;
import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.service.RelationshipService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/relationship")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public List<RelationshipResponseDTO> getAllRelationships() {
        List<Relationship> relationshipList = relationshipService.getAllRelationships();
        return relationshipList.stream()
                .map(DomainMapper::toResponseDTO)
                .toList();
    }

    @GetMapping("/{relationshipId}")
    public RelationshipResponseDTO getRelationship(@Positive @PathVariable Integer relationshipId) {
        Relationship relationship = relationshipService.getRelationshipById(relationshipId);
        return DomainMapper.toResponseDTO(relationship);
    }

    @GetMapping("/{relationshipId}/birth-record/{category}/{grade1}/{grade2}")
    public List<BirthRecordDTO> getBirthRecordsForCategoryAndEpoch(@PathVariable Integer relationshipId, @PathVariable Category category, @PathVariable Grade grade1, @PathVariable Grade grade2) {
        List<BirthRecord> birthRecords = relationshipService.findBirthRecordsByCategoryAndEpoch(relationshipId, category, new GradePair(grade1, grade2));
        return birthRecords.stream()
                .map(DomainMapper::toResponseDTO)
                .toList();
    }

}
