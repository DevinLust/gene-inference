package com.progressengine.geneinference.exception;

import com.progressengine.geneinference.model.ExcessAlleleViolation;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExcessAlleleDiversityException extends GeneticConstraintViolationException {

    private final Set<Grade> oldAlleleSet;
    private final Grade newAllele;
    private final Category category;
    private final List<ExcessAlleleViolation> violations;

    public ExcessAlleleDiversityException(String message, Set<Grade> oldAlleleSet, Grade newAllele, Category category) {
        super(message);
        this.oldAlleleSet = oldAlleleSet;
        this.newAllele = newAllele;
        this.category = category;
        this.violations = new ArrayList<>();
    }

    public ExcessAlleleDiversityException(String message, List<ExcessAlleleViolation> violations) {
        super(message);
        this.violations = violations;
        this.oldAlleleSet = null;
        this.newAllele = null;
        this.category = null;
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

    public List<ExcessAlleleViolation> getViolations() {
        return violations;
    }
}
