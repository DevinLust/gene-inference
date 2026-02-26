package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.BirthRecordDTO;
import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.Relationship;
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
        Relationship relationship = relationshipService.getRelationshipWithBirthsById(relationshipId);
        return DomainMapper.toResponseDTO(relationship);
    }

}
