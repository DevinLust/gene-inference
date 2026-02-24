package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.mapper.DomainMapper;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.service.RelationshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/birth-record")
public class BirthRecordController {

    private final RelationshipService relationshipService;

    public BirthRecordController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping("/{brId}")
    public ResponseEntity<?> getBirthRecordById(@PathVariable Integer brId) {
        BirthRecord br = relationshipService.findBirthRecordById(brId);
        return ResponseEntity.ok(DomainMapper.toResponseDTO(br));
    }

    @DeleteMapping("/{brId}")
    public ResponseEntity<?> deleteById(@PathVariable Integer brId) {
        relationshipService.deleteBirthRecord(brId);
        return ResponseEntity.noContent().build();
    }
}
