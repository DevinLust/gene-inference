package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;

import java.io.Serializable;
import java.util.Objects;

public class BirthRecordPhenotypeKey implements Serializable {
    private Integer birthRecord;
    private Category category;

    public BirthRecordPhenotypeKey() {}

    public BirthRecordPhenotypeKey(Integer birthRecord, Category category) {
        this.birthRecord = birthRecord;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BirthRecordPhenotypeKey that)) return false;
        return Objects.equals(birthRecord, that.birthRecord) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(birthRecord, category);
    }
}
