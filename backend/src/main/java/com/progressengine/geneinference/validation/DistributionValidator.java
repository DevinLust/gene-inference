package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

public class DistributionValidator
        implements ConstraintValidator<ValidDistribution, Map<Category, Map<Grade, Double>>> {

    @Override
    public boolean isValid(Map<Category, Map<Grade, Double>> dto, ConstraintValidatorContext ctx) {
        if (dto == null || dto.isEmpty()) {
            return true; // optional field
        }

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        HibernateConstraintValidatorContext hctx =
                ctx.unwrap(HibernateConstraintValidatorContext.class);

        for (Map.Entry<Category, Map<Grade, Double>> entry : dto.entrySet()) {
            Category category = entry.getKey();
            Map<Grade, Double> dist = entry.getValue();

            // Category-level path: distributions[CAT]
            if (dist == null) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Distribution is null (all grades missing)")
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
                continue;
            }

            // Missing grades => attach to distributions[CAT] (or per-grade if you prefer)
            EnumSet<Grade> missingGrades = EnumSet.allOf(Grade.class);
            missingGrades.removeAll(dist.keySet());
            if (!missingGrades.isEmpty()) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Missing grades: " + missingGrades)
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
            }

            // Grade-level path: distributions[CAT][GRADE]
            for (Map.Entry<Grade, Double> gentry : dist.entrySet()) {
                Grade grade = gentry.getKey();
                Double vObj = gentry.getValue();
                if (vObj == null) {
                    valid = false;
                    hctx.buildConstraintViolationWithTemplate("Value is required")
                            .addBeanNode()
                            .inIterable().atKey(category)
                            .addConstraintViolation();
                    continue;
                }

                double v = vObj;
                if (v < 0.0 || v > 1.0) {
                    valid = false;
                    hctx.buildConstraintViolationWithTemplate("Value for " + grade + " out of range [0,1]: " + v)
                            .addBeanNode()
                            .inIterable().atKey(category)
                            .addConstraintViolation();
                }
            }

            // Sum error => attach to distributions[CAT]
            double sum = dist.values().stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .sum();

            if (Math.abs(sum - 1.0) > 1e-6) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Probabilities sum to " + sum + ", must sum to 1")
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
            }
        }

        return valid;
    }
}
