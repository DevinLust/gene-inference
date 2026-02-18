package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.service.BreedingService;
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

    @DeleteMapping("/{brId}")
    public ResponseEntity<?> deleteById(@PathVariable Integer brId) {
        relationshipService.deleteBirthRecord(brId);
        return ResponseEntity.noContent().build();
    }
}
