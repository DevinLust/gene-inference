package com.progressengine.geneinference.exception;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.Set;

public class ExcessAlleleDiversityException extends GeneticConstraintViolationException {

    private final Set<Grade> oldAlleleSet;
    private final Grade newAllele;
    private final Category category;

    public ExcessAlleleDiversityException(String message, Set<Grade> oldAlleleSet, Grade newAllele, Category category) {
        super(message);
        this.oldAlleleSet = oldAlleleSet;
        this.newAllele = newAllele;
        this.category = category;
    }

    public Set<Grade> getOldAlleleSet() {
        return oldAlleleSet;
    }

    public Grade getNewAllele() {
        return newAllele;
    }

    public Category getCategory() {
        return category;
    }
}
