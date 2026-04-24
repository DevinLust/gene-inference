package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepGenotypeDTO;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Map;

public class GenotypePatchValidator
        implements ConstraintValidator<ValidGenotypePatch, Map<Category, SheepGenotypeDTO>> {

    @Override
    public boolean isValid(
            Map<Category, SheepGenotypeDTO> genotypes,
            ConstraintValidatorContext ctx
    ) {
        // PATCH semantics: null means "no genotype updates"
        if (genotypes == null) {
            return true;
        }

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        HibernateConstraintValidatorContext hctx =
                ctx.unwrap(HibernateConstraintValidatorContext.class);

        for (Map.Entry<Category, SheepGenotypeDTO> entry : genotypes.entrySet()) {
            Category category = entry.getKey();
            SheepGenotypeDTO dto = entry.getValue();

            if (category == null) {
                hctx.buildConstraintViolationWithTemplate("Category cannot be null")
                        .addBeanNode()
                        .addConstraintViolation();
                valid = false;
                continue;
            }

            // PATCH semantics: null category value means "no change"
            if (dto == null) {
                continue;
            }

            String phenotypeCode = normalize(dto.phenotype());
            String hiddenCode = normalize(dto.hiddenAllele());

            // PATCH semantics: both null means "no change"
            if (phenotypeCode == null && hiddenCode == null) {
                continue;
            }

            AlleleDomain<?> domain = CategoryDomains.domainFor(category);

            Object phenotype = null;
            if (phenotypeCode != null) {
                try {
                    phenotype = domain.parse(phenotypeCode);
                } catch (IllegalArgumentException ex) {
                    addViolation(hctx, category, "Invalid phenotype code for category "
                            + category + ": " + phenotypeCode);
                    valid = false;
                }
            }

            Object hidden = null;
            if (hiddenCode != null) {
                try {
                    hidden = domain.parse(hiddenCode);
                } catch (IllegalArgumentException ex) {
                    addViolation(hctx, category, "Invalid hidden allele code for category "
                            + category + ": " + hiddenCode);
                    valid = false;
                }
            }

            /*
             * Only check phenotype/hidden compatibility here when both are supplied
             * in the patch. If only hidden is supplied, the service must validate it
             * against the existing phenotype on the Sheep entity.
             */
            if (phenotype != null && hidden != null) {
                valid = validateCompatibilityTyped(
                        category,
                        phenotypeCode,
                        hiddenCode,
                        hctx
                ) && valid;
            }
        }

        return valid;
    }

    private static String normalize(String code) {
        if (code == null) return null;
        String trimmed = code.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void addViolation(
            HibernateConstraintValidatorContext hctx,
            Category category,
            String message
    ) {
        hctx.buildConstraintViolationWithTemplate(message)
                .addBeanNode()
                .inIterable().atKey(category)
                .addConstraintViolation();
    }

    private <A extends Enum<A> & com.progressengine.geneinference.model.enums.Allele>
    boolean validateCompatibilityTyped(
            Category category,
            String phenotypeCode,
            String hiddenCode,
            HibernateConstraintValidatorContext hctx
    ) {
        AlleleDomain<A> domain = CategoryDomains.typedDomainFor(category);

        A phenotype = domain.parse(phenotypeCode);
        A hidden = domain.parse(hiddenCode);

        if (!domain.isHiddenAllelePossible(phenotype, hidden)) {
            addViolation(
                    hctx,
                    category,
                    "Hidden allele is inconsistent with the phenotype for category "
                            + category + ": (" + phenotypeCode + ", " + hiddenCode + ").\n"
                            + "The hidden allele would be expressed instead due to dominance rules."
            );
            return false;
        }

        return true;
    }
}
