package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepRequestDTO;
import com.progressengine.geneinference.dto.SheepResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Sheep> getAllSheep() {
        return sheepRepository.findAll();
    }

    public Sheep fromRequestDTO(SheepRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setPhenotype(dto.getPhenotype());
        sheep.setHiddenAllele(dto.getHiddenAllele());

        if (dto.getHiddenDistribution() == null || dto.getHiddenDistribution().size() != Grade.values().length) {
            throw new IllegalArgumentException("hiddenDistribution must include all grades");
        }
        sheep.setHiddenDistribution(dto.getHiddenDistribution());

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
        responseDTO.setPhenotype(sheep.getPhenotype());
        responseDTO.setHiddenAllele(sheep.getHiddenAllele());
        responseDTO.setHiddenDistribution(sheep.getHiddenDistribution());

        if (sheep.getParentRelationship() != null) {
            responseDTO.setParentRelationshipId(sheep.getParentRelationship().getId());
        }

        return responseDTO;
    }
}
