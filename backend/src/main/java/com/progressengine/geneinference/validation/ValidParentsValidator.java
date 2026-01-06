package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepNewRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidParentsValidator
        implements ConstraintValidator<ValidParents, SheepNewRequestDTO> {

    @Override
    public boolean isValid(
            SheepNewRequestDTO dto,
            ConstraintValidatorContext context
    ) {
        Integer p1 = dto.getParent1Id();
        Integer p2 = dto.getParent2Id();

        // Case 1: neither provided → valid
        if (p1 == null && p2 == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        // Case 2: only one provided → invalid
        if (p1 == null || p2 == null) {
            context.buildConstraintViolationWithTemplate(
                            "Both parent1Id and parent2Id must be provided together"
                    ).addPropertyNode(p1 == null ? "parent1Id" : "parent2Id")
                    .addConstraintViolation();
            return false;
        }

        // Case 3: same ID → invalid
        if (p1.equals(p2)) {
            context.buildConstraintViolationWithTemplate(
                            "parent1Id and parent2Id must be different"
                    ).addPropertyNode("parent2Id")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

