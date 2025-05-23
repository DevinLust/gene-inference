package com.progressengine.geneinference.service;

import com.progressengine.geneinference.dto.SheepRequestDTO;
import com.progressengine.geneinference.dto.SheepResponseDTO;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.model.enums.Grade;
import com.progressengine.geneinference.repository.RelationshipRepository;
import com.progressengine.geneinference.repository.SheepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SheepService {

    private final SheepRepository sheepRepository;
    private final RelationshipRepository relationshipRepository;

    public SheepService(RelationshipRepository relationshipRepository, SheepRepository sheepRepository) {
        this.relationshipRepository = relationshipRepository;
        this.sheepRepository = sheepRepository;
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
