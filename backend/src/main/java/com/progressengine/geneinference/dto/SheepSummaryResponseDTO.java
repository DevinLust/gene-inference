package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.Sheep;

public class SheepSummaryResponseDTO {
    private final Integer id;
    private final String name;

    public SheepSummaryResponseDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
