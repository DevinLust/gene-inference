package com.progressengine.geneinference.mapper;

import com.progressengine.geneinference.dto.*;
import com.progressengine.geneinference.model.BirthRecord;
import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.Sheep;

public class DomainMapper {
    public static Sheep fromRequestDTO(SheepNewRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        return sheep;
    }

    public static Sheep fromRequestDTO(SheepBreedRequestDTO dto) {
        Sheep sheep = new Sheep();
        sheep.setName(dto.getName());
        sheep.setGenotypes(dto.getGenotypes());

        sheep.upsertDistributionsFromDTO(dto.getDistributions());

        return sheep;
    }

    public static SheepResponseDTO toResponseDTO(Sheep sheep) {
        SheepResponseDTO responseDTO = new SheepResponseDTO();
        responseDTO.setId(sheep.getId());
        responseDTO.setName(sheep.getName());
        responseDTO.setGenotypes(sheep.getGenotypes());
        responseDTO.setDistributions(sheep.getAllDistributions());

        if (sheep.getParentRelationship() != null) {
            responseDTO.setParentRelationshipId(sheep.getParentRelationship().getId());
        }

        return responseDTO;
    }

    public static RelationshipResponseDTO toResponseDTO(Relationship relationship) {
        Sheep parent1 = relationship.getParent1();
        Sheep parent2 = relationship.getParent2();
        SheepSummaryResponseDTO parent1Summary = new SheepSummaryResponseDTO(parent1.getId(), parent1.getName());
        SheepSummaryResponseDTO parent2Summary = new SheepSummaryResponseDTO(parent2.getId(), parent2.getName());
        return new RelationshipResponseDTO(relationship.getId(), parent1Summary, parent2Summary, relationship.getFrequenciesExperimental());
    }

    public static BirthRecordDTO toResponseDTO(BirthRecord birthRecord) {
        BirthRecordDTO dto = new BirthRecordDTO();
        dto.setId(birthRecord.getId());
        dto.setParentRelationshipId(birthRecord.getParentRelationship().getId());
        if (birthRecord.getChild() != null) {
            dto.setChildId(birthRecord.getChild().getId());
        }
        dto.setPhenotypesAtBirth(birthRecord.getPhenotypesAtBirthOrganized());
        return dto;
    }
}
