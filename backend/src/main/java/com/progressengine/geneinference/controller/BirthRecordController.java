package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.BirthRecordRow;
import com.progressengine.geneinference.dto.BirthRecordSearchParams;
import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.service.RelationshipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/birth-record")
public class BirthRecordController {

    private final RelationshipService relationshipService;

    public BirthRecordController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public ResponseEntity<?> listBirthRecords(@ModelAttribute BirthRecordSearchParams params,
                                              Pageable pageable,
                                              @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Page<BirthRecordRow> page = relationshipService.searchBirthRecords(userId, params, pageable);
        return ResponseEntity.ok(DomainMapper.toResponseDTO(page));
    }

    @GetMapping("/{brId}")
    public ResponseEntity<?> getBirthRecordById(@PathVariable Integer brId,
                                                @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        BirthRecord br = relationshipService.findBirthRecordByIdAndUserId(userId, brId);
        return ResponseEntity.ok(DomainMapper.toResponseDTO(br));
    }

    @DeleteMapping("/{brId}")
    public ResponseEntity<?> deleteById(@PathVariable Integer brId,
                                        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        relationshipService.deleteBirthRecord(userId, brId);
        return ResponseEntity.noContent().build();
    }
}
