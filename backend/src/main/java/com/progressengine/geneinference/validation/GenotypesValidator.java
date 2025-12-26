package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Map;

public class GenotypesValidator implements ConstraintValidator<ValidGenotypes, Map<Category, SheepGenotypeDTO>> {

    @Override
    public boolean isValid(Map<Category, SheepGenotypeDTO> genotypes, ConstraintValidatorContext ctx) {
        if (genotypes == null) {
            return false; // @NotNull should handle null, but we can enforce here as well
        }

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        // Check all categories are present
        EnumSet<Category> missingCategories = EnumSet.allOf(Category.class);
        missingCategories.removeAll(genotypes.keySet());
        if (!missingCategories.isEmpty()) {
            ctx.buildConstraintViolationWithTemplate("Missing categories: " + missingCategories)
                    .addConstraintViolation();
            valid = false;
        }

        // Check each SheepGenotypeDTO for non-null phenotype
        for (Map.Entry<Category, SheepGenotypeDTO> entry : genotypes.entrySet()) {
            Category category = entry.getKey();
            SheepGenotypeDTO dto = entry.getValue();
            if (dto == null) {
                ctx.buildConstraintViolationWithTemplate("Category " + category + " has null value")
                        .addConstraintViolation();
                valid = false;
                continue;
            }
            if (dto.phenotype() == null) {
                ctx.buildConstraintViolationWithTemplate("Category " + category + " has null phenotype")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
