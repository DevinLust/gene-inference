package com.progressengine.geneinference.exception;

import com.progressengine.geneinference.model.ExcessAlleleViolation;
import com.progressengine.geneinference.model.enums.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExcessAlleleDiversityException extends GeneticConstraintViolationException {

    private final Set<String> oldAlleleSet;
    private final String newAllele;
    private final Category category;
    private final List<ExcessAlleleViolation> violations;

    public ExcessAlleleDiversityException(String message, Set<String> oldAlleleSet, String newAllele, Category category) {
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

    public Set<String> getOldAlleleSet() {
        return oldAlleleSet;
    }

    public String getNewAllele() {
        return newAllele;
    }

    public Category getCategory() {
        return category;
    }

    public List<ExcessAlleleViolation> getViolations() {
        return violations;
    }
}
