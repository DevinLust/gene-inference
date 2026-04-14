package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Map;

public class GenotypesValidator implements ConstraintValidator<ValidGenotypes, Map<Category, SheepGenotypeDTO>> {

    @Override
    public boolean isValid(Map<Category, SheepGenotypeDTO> genotypes, ConstraintValidatorContext ctx) {
        if (genotypes == null) {
            return false;
        }

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        HibernateConstraintValidatorContext hctx =
                ctx.unwrap(HibernateConstraintValidatorContext.class);

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

            if (cat == null) {
                hctx.buildConstraintViolationWithTemplate("Category cannot be null")
                        .addBeanNode()
                        .addConstraintViolation();
                valid = false;
                continue;
            }

            if (dto == null) {
                hctx.buildConstraintViolationWithTemplate("Null genotype value")
                        .addBeanNode()
                        .inIterable().atKey(cat)
                        .addConstraintViolation();
                valid = false;
                continue;
            }

            if (dto.phenotype() == null) {
                hctx.buildConstraintViolationWithTemplate("Phenotype is required")
                        .addBeanNode()
                        .inIterable().atKey(cat)
                        .addConstraintViolation();
                valid = false;
            } else {
                boolean phenotypeValid = validateCode(cat, dto.phenotype(), "Invalid phenotype code", hctx);
                valid = phenotypeValid && valid;

                boolean hiddenValid = true;
                if (dto.hiddenAllele() != null) {
                    hiddenValid = validateCode(cat, dto.hiddenAllele(), "Invalid hidden allele code", hctx);
                    valid = hiddenValid && valid;
                }

                // 🔥 NEW: only check compatibility if both parsed successfully
                if (phenotypeValid && hiddenValid && dto.hiddenAllele() != null) {
                    valid = validateHiddenCompatibility(cat, dto.phenotype(), dto.hiddenAllele(), hctx) && valid;
                }
            }
        }

        return valid;
    }

    private boolean validateCode(
            Category category,
            String code,
            String messagePrefix,
            HibernateConstraintValidatorContext hctx
    ) {
        try {
            AlleleDomain<?> domain = CategoryDomains.domainFor(category);
            domain.parse(code);
            return true;
        } catch (IllegalArgumentException ex) {
            hctx.buildConstraintViolationWithTemplate(messagePrefix + " for category " + category + ": " + code)
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
            return false;
        }
    }

    private boolean validateHiddenCompatibility(
            Category category,
            String phenotypeCode,
            String hiddenCode,
            HibernateConstraintValidatorContext hctx
    ) {
        try {
            AlleleDomain<?> domain = CategoryDomains.domainFor(category);
            return validateHiddenCompatibilityTyped(
                    domain,
                    category,
                    phenotypeCode,
                    hiddenCode,
                    hctx
            );
        } catch (IllegalArgumentException ex) {
            // parse should already have been validated earlier, so this is just a safety fallback
            hctx.buildConstraintViolationWithTemplate(
                            "Hidden allele is not compatible with phenotype for category "
                                    + category + ": (" + phenotypeCode + ", " + hiddenCode + ")"
                    )
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
            return false;
        }
    }

    private <A extends Enum<A> & Allele> boolean validateHiddenCompatibilityTyped(
            AlleleDomain<A> domain,
            Category category,
            String phenotypeCode,
            String hiddenCode,
            HibernateConstraintValidatorContext hctx
    ) {
        A phenotype = domain.parse(phenotypeCode);
        A hidden = domain.parse(hiddenCode);

        if (!domain.isHiddenAllelePossible(phenotype, hidden)) {
            hctx.buildConstraintViolationWithTemplate(
                            "Hidden allele is not compatible with phenotype for category "
                                    + category + ": (" + phenotypeCode + ", " + hiddenCode + ")"
                    )
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
