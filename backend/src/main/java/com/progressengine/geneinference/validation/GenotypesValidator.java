package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Map;

public class GenotypesValidator implements ConstraintValidator<ValidGenotypes, Map<Category, SheepGenotypeDTO>> {

    @Override
    public boolean isValid(Map<Category, SheepGenotypeDTO> genotypes, ConstraintValidatorContext ctx) {
        if (genotypes == null) return false;

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        HibernateConstraintValidatorContext hctx =
                ctx.unwrap(HibernateConstraintValidatorContext.class);

        // Missing categories => genotypes[CAT]
        EnumSet<Category> missing = EnumSet.allOf(Category.class);
        missing.removeAll(genotypes.keySet());
        for (Category cat : missing) {
            hctx.buildConstraintViolationWithTemplate("Missing category")
                    .addBeanNode()
                    .inIterable().atKey(cat)
                    .addConstraintViolation();
            valid = false;
        }

        for (Map.Entry<Category, SheepGenotypeDTO> e : genotypes.entrySet()) {
            Category cat = e.getKey();
            SheepGenotypeDTO dto = e.getValue();

            if (dto == null) {
                hctx.buildConstraintViolationWithTemplate("Null genotype value")
                        .addBeanNode()
                        .inIterable().atKey(cat)
                        .addConstraintViolation();
                valid = false;
                continue;
            }

            if (dto.phenotype() == null) {
                // Attach error to the category key (genotypes[CAT])
                hctx.buildConstraintViolationWithTemplate("Phenotype is required")
                        .addBeanNode()
                        .inIterable().atKey(cat)
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}

