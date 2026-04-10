package com.progressengine.geneinference.model;

public record PhenotypeAtBirth(String parent1Code, String parent2Code, String childCode) {
    public PhenotypeAtBirth(BirthRecordPhenotype birthRecordPhenotype) {
        this(
                birthRecordPhenotype.getParent1PhenotypeCode(),
                birthRecordPhenotype.getParent2PhenotypeCode(),
                birthRecordPhenotype.getChildPhenotypeCode()
        );
    }
}
