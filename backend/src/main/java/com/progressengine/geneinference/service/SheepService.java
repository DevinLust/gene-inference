package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepNewRequestDTO;
import com.progressengine.geneinference.dto.SheepReplaceRequestDTO;
import com.progressengine.geneinference.dto.SheepResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SheepService {

    private final SheepRepository sheepRepository;
    private final RelationshipRepository relationshipRepository;

    public static Map<Grade, Double> createUniformDistribution() {
        Map<Grade, Double> uniformDistribution = new EnumMap<>(Grade.class);
        int totalGrades = Grade.values().length;
        double probability = 1.0 / totalGrades;

        for (Grade grade : Grade.values()) {
            uniformDistribution.put(grade, probability);
        }

        return uniformDistribution;
    }

    public SheepService(RelationshipRepository relationshipRepository, SheepRepository sheepRepository) {
        this.relationshipRepository = relationshipRepository;
        this.sheepRepository = sheepRepository;
    }

    public Sheep findById(Integer id) {
        Sheep sheep = null;

        Optional<Sheep> possibleSheep = sheepRepository.findById(id);
        if (possibleSheep.isPresent()) {
            sheep = possibleSheep.get();
        }

        return sheep;
    }

    public Sheep saveSheep(Sheep sheep) {
        return sheepRepository.save(sheep);
    }

    public void saveAll(List<Sheep> sheep) {
        sheepRepository.saveAll(sheep);
    }

    public List<Sheep> getAllSheep() {
        return sheepRepository.findAll();
    }

    public Sheep getSheepById(Integer id) {
        Optional<Sheep> possibleSheep = sheepRepository.findById(id);
        if (possibleSheep.isEmpty()) {
            throw new OpenApiResourceNotFoundException("Sheep not found");
        }
        return possibleSheep.get();
    }

    public ResponseEntity<?> getParents(Integer sheepId) {
        return sheepRepository.findById(sheepId)
                .map(sheep -> {
                    if (sheep.getParentRelationship() == null) {
                        return ResponseEntity.noContent().build();
                    }

                    // Assuming parentRelationshipId encodes a valid Relationship
                    Relationship rel = sheep.getParentRelationship();

                    List<Sheep> parents = sheepRepository.findAllById(
                            List.of(rel.getParent1().getId(), rel.getParent2().getId())
                    );

                    return ResponseEntity.ok(Map.of(
                            "parent1", toResponseDTO(parents.get(0)),
                            "parent2", toResponseDTO(parents.get(1))
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> getChildren(Integer sheepId) {
        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);

        if (relationships.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> relationshipIds = relationships.stream().map(Relationship::getId).toList();
        List<SheepResponseDTO> children = sheepRepository.findAllByParentRelationship_IdIn(relationshipIds).stream().map(this::toResponseDTO).toList();

        return ResponseEntity.ok(children);
    }

    public ResponseEntity<?> getPartners(Integer sheepId) {
        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);

        if (relationships.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<SheepResponseDTO> partners = relationships.stream()
                .map(rel -> {
                    if (rel.getParent1().getId().equals(sheepId)) {
                        return toResponseDTO(rel.getParent2());
                    } else {
                        return toResponseDTO(rel.getParent1());
                    }
                }).toList();

        return ResponseEntity.ok(partners);
    }

    public void deleteSheep(Integer sheepId) {
        Sheep sheep = findById(sheepId);

        List<Relationship> relationships = relationshipRepository.findByParentId(sheepId);
        Set<Integer> relationshipIds = relationships.stream().map(Relationship::getId).collect(Collectors.toSet());

        List<Sheep> affectedChildren = sheepRepository.findAllByParentRelationship_IdIn(relationshipIds);
        for (Sheep child : affectedChildren) {
            child.setParentRelationship(null);
        }

        relationshipRepository.deleteAll(relationships);

        sheepRepository.delete(sheep);
    }

    public Sheep fromRequestDTO(SheepNewRequestDTO dto) throws IllegalArgumentException {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        if (dto.getParentRelationshipId() != null) {
            Relationship relationship = relationshipRepository.findById(dto.getParentRelationshipId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parentRelationshipId: " + dto.getParentRelationshipId()));
            sheep.setParentRelationship(relationship);
        }

        return sheep;
    }

    public Sheep fromRequestDTO(SheepReplaceRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        if (dto.getParentRelationshipId() != null) {
            Relationship relationship = relationshipRepository.findById(dto.getParentRelationshipId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid parentRelationshipId: " + dto.getParentRelationshipId()));
            sheep.setParentRelationship(relationship);
        }

        return sheep;
    }

    public SheepResponseDTO toResponseDTO(Sheep sheep) {
        SheepResponseDTO responseDTO = new SheepResponseDTO();
        responseDTO.setId(sheep.getId());
        responseDTO.setName(sheep.getName());
        responseDTO.setGenotypes(sheep.getGenotypes());
        responseDTO.setDistributionsByCategory(sheep.getAllDistributions());

        if (sheep.getParentRelationship() != null) {
            responseDTO.setParentRelationshipId(sheep.getParentRelationship().getId());
        }

        return responseDTO;
    }
}
