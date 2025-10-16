package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.Sheep;

public class SheepSummaryResponseDTO {
    private final Integer id;
    private final String name;

    public SheepSummaryResponseDTO(Sheep sheep) {
        this.id = sheep.getId();
        this.name = sheep.getName();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
