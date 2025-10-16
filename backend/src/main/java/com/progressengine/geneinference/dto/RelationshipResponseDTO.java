package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.Relationship;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Map;

public record RelationshipResponseDTO(
        Integer id,
        Integer parent1Id,
        Integer parent2Id,
        Map<Category, Map<Grade, Integer>> phenotypeFrequencies
) {
    public RelationshipResponseDTO(Relationship relationship) {
        this(
                relationship.getId(),
                relationship.getParent1().getId(),
                relationship.getParent2().getId(),
                relationship.getAllPhenotypeFrequencies()
        );
    }
}
