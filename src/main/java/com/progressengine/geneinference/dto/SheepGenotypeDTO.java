package com.progressengine.geneinference.dto;

import com.progressengine.geneinference.model.enums.Grade;

public class SheepGenotypeDTO {
    private Grade phenotype;
    private Grade hiddenAllele;

    public SheepGenotypeDTO(Grade phenotype, Grade hiddenAllele) {
        this.phenotype = phenotype;
        this.hiddenAllele = hiddenAllele;
    }

    public Grade getPhenotype() {
        return phenotype;
    }

    public Grade getHiddenAllele() {
        return hiddenAllele;
    }
}

