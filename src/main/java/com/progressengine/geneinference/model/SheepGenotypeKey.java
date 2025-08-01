package com.progressengine.geneinference.model;

import com.progressengine.geneinference.model.enums.Category;

import java.io.Serializable;
import java.util.Objects;

public class SheepGenotypeKey implements Serializable {
    private Integer sheep;
    private Category category;

    public SheepGenotypeKey() {}

    public SheepGenotypeKey(Integer sheep, Category category) {
        this.sheep = sheep;
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SheepGenotypeKey that)) return false;
        return Objects.equals(sheep, that.sheep) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheep, category);
    }
}

