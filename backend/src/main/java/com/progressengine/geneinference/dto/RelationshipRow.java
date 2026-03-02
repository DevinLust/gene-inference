package com.progressengine.geneinference.dto;

public record RelationshipRow(
        Integer id,
        Integer parent1Id,
        String parent1Name,
        Integer parent2Id,
        String parent2Name
) {}
