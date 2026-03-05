package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.BirthRecordDTO;
import com.progressengine.geneinference.dto.RelationshipResponseDTO;
import com.progressengine.geneinference.dto.RelationshipRow;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.service.RelationshipService;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Validated
@RequestMapping(value = "/relationship")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public List<RelationshipRow> getAllRelationships(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return relationshipService.getAllRelationshipRows(userId);
    }

    @GetMapping("/{relationshipId}")
    public RelationshipResponseDTO getRelationship(@Positive @PathVariable Integer relationshipId,
                                                   @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Relationship relationship = relationshipService.getRelationshipWithBirthsById(userId, relationshipId);
        return DomainMapper.toResponseDTO(relationship);
    }

}
