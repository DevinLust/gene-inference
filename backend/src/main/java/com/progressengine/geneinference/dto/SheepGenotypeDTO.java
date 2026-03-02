package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.GradePair;
import com.progressengine.geneinference.model.enums.Grade;

public record SheepGenotypeDTO(Grade phenotype, Grade hiddenAllele) {

    public GradePair toGradePair() {
        return new GradePair(phenotype, hiddenAllele);
    }
}

