package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.service.RelationshipService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                .map(relationshipService::toResponseDTO)
                .toList();
    }

    @GetMapping("/{relationshipId}")
    public RelationshipResponseDTO getRelationship(@Positive @PathVariable Integer relationshipId) {
        Relationship relationship = relationshipService.getRelationshipById(relationshipId);
        return relationshipService.toResponseDTO(relationship);
    }

}
