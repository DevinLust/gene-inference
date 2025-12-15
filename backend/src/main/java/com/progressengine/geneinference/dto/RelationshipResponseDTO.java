package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public record RelationshipResponseDTO(
        Integer id,
        SheepSummaryResponseDTO parent1,
        SheepSummaryResponseDTO parent2,
        Map<Category, Map<Grade, Integer>> phenotypeFrequencies
) {
    public RelationshipResponseDTO(Relationship relationship) {
        this(
                relationship.getId(),
                new SheepSummaryResponseDTO(relationship.getParent1()),
                new SheepSummaryResponseDTO(relationship.getParent2()),
                relationship.getAllPhenotypeFrequencies()
        );
    }
}
