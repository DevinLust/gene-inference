package com.progressengine.geneinference.dto;

public record RelationshipSummaryResponseDTO(
        Integer id,
        SheepSummaryResponseDTO parent1,
        SheepSummaryResponseDTO parent2
) {}
