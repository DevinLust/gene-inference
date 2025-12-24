package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.model.enums.Grade;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;
import java.util.Map;

public class DistributionValidator
        implements ConstraintValidator<ValidDistribution, Map<Category, Map<Grade, Double>>> {

    @Override
    public boolean isValid(Map<Category, Map<Grade, Double>> dto, ConstraintValidatorContext ctx) {
        if (dto == null || dto.isEmpty()) {
            return true; // optional field
        }

        boolean valid = true;

        // disable default message
        ctx.disableDefaultConstraintViolation();

        for (Map.Entry<Category, Map<Grade, Double>> entry : dto.entrySet()) {
            Category category = entry.getKey();
            Map<Grade, Double> dist = entry.getValue();

            if (dist == null) {
                // Treat null map as missing all grades
                valid = false;
                ctx.buildConstraintViolationWithTemplate(
                        "Category " + category + " distribution is null (all grades missing)"
                ).addConstraintViolation();
                continue; // move on to next category
            }

            // 1️⃣ Check all grades present
            EnumSet<Grade> missingGrades = EnumSet.allOf(Grade.class);
            missingGrades.removeAll(dist.keySet());
            if (!missingGrades.isEmpty()) {
                valid = false;
                ctx.buildConstraintViolationWithTemplate(
                        "Category " + category + " missing grades: " + missingGrades
                ).addConstraintViolation();
            }

            // 2️⃣ Check values in range
            for (Map.Entry<Grade, Double> gentry : dist.entrySet()) {
                double v = gentry.getValue();
                if (v < 0.0 || v > 1.0) {
                    valid = false;
                    ctx.buildConstraintViolationWithTemplate(
                            "Category " + category + ", grade " + gentry.getKey() +
                                    " has value out of range [0,1]: " + v
                    ).addConstraintViolation();
                }
            }

            // 3️⃣ Check sum ≈ 1
            double sum = dist.values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sum - 1.0) > 1e-6) {
                valid = false;
                ctx.buildConstraintViolationWithTemplate(
                        "Category " + category + " probabilities sum to " + sum + ", must sum to 1"
                ).addConstraintViolation();
            }
        }

        return valid;
    }
}
