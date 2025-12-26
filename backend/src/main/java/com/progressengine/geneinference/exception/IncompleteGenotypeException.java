package com.progressengine.geneinference.exception;

import com.progressengine.geneinference.model.enums.Category;

import java.util.Set;

public class IncompleteGenotypeException extends RuntimeException {
    private final Set<Category> parent1Missing;
    private final Set<Category> parent2Missing;

    public IncompleteGenotypeException(
            Set<Category> parent1Missing,
            Set<Category> parent2Missing
    ) {
        super("Cannot breed sheep with missing hidden alleles");
        this.parent1Missing = parent1Missing;
        this.parent2Missing = parent2Missing;
    }

    public Set<Category> getParent1Missing() { return parent1Missing; }
    public Set<Category> getParent2Missing() { return parent2Missing; }
}

