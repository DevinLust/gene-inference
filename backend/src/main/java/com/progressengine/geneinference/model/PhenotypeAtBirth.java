package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Grade;

public record PhenotypeAtBirth(Grade parent1, Grade parent2, Grade child) {

    public PhenotypeAtBirth(Grade parent1, Grade parent2, Grade child) {
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.child = child;
    }

    public PhenotypeAtBirth(BirthRecordPhenotype birthRecordPhenotype) {
        this(
                birthRecordPhenotype.getParent1Phenotype(),
                birthRecordPhenotype.getParent2Phenotype(),
                birthRecordPhenotype.getChildPhenotype()
        );
    }
}
