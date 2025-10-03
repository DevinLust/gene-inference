package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.service.RelationshipService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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

}
