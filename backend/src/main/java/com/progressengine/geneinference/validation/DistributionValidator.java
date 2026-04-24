package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.model.enums.Allele;
import com.progressengine.geneinference.model.enums.Category;
import com.progressengine.geneinference.service.AlleleDomains.AlleleDomain;
import com.progressengine.geneinference.service.AlleleDomains.CategoryDomains;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DistributionValidator
        implements ConstraintValidator<ValidDistribution, Map<Category, Map<String, Double>>> {

    private static final double EPSILON = 1e-6;

    @Override
    public boolean isValid(Map<Category, Map<String, Double>> dto, ConstraintValidatorContext ctx) {
        if (dto == null || dto.isEmpty()) {
            return true;
        }

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        HibernateConstraintValidatorContext hctx =
                ctx.unwrap(HibernateConstraintValidatorContext.class);

        for (Map.Entry<Category, Map<String, Double>> entry : dto.entrySet()) {
            Category category = entry.getKey();
            Map<String, Double> dist = entry.getValue();

            if (category == null) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Category cannot be null")
                        .addBeanNode()
                        .addConstraintViolation();
                continue;
            }

            if (dist == null) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Distribution is null")
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
                continue;
            }

            valid = validateDistributionForCategory(category, dist, hctx) && valid;
        }

        return valid;
    }

    private boolean validateDistributionForCategory(
            Category category,
            Map<String, Double> dist,
            HibernateConstraintValidatorContext hctx
    ) {
        boolean valid = true;

        AlleleDomain<?> domain = CategoryDomains.domainFor(category);
        Set<String> expectedCodes = domain.getAlleles().stream()
                .map(Allele::code)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> actualCodes = dist.keySet().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> missingCodes = new LinkedHashSet<>(expectedCodes);
        missingCodes.removeAll(actualCodes);
        if (!missingCodes.isEmpty()) {
            valid = false;
            hctx.buildConstraintViolationWithTemplate("Missing alleles: " + missingCodes)
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
        }

        Set<String> extraCodes = new LinkedHashSet<>(actualCodes);
        extraCodes.removeAll(expectedCodes);
        if (!extraCodes.isEmpty()) {
            valid = false;
            hctx.buildConstraintViolationWithTemplate("Invalid alleles: " + extraCodes)
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
        }

        for (Map.Entry<String, Double> alleleEntry : dist.entrySet()) {
            String alleleCode = alleleEntry.getKey();
            Double valueObj = alleleEntry.getValue();

            if (alleleCode == null) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Allele code cannot be null")
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
                continue;
            }

            if (valueObj == null) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate("Value is required for allele " + alleleCode)
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
                continue;
            }

            double value = valueObj;
            if (value < 0.0 || value > 1.0) {
                valid = false;
                hctx.buildConstraintViolationWithTemplate(
                                "Value for allele " + alleleCode + " out of range [0,1]: " + value
                        )
                        .addBeanNode()
                        .inIterable().atKey(category)
                        .addConstraintViolation();
            }
        }

        double sum = dist.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(sum - 1.0) > EPSILON) {
            valid = false;
            hctx.buildConstraintViolationWithTemplate(
                            "Probabilities sum to " + sum + ", must sum to 1"
                    )
                    .addBeanNode()
                    .inIterable().atKey(category)
                    .addConstraintViolation();
        }

        return valid;
    }
}