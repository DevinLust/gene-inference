package com.progressengine.geneinference.validation;

import com.progressengine.geneinference.dto.SheepBreedRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidParentsValidator
        implements ConstraintValidator<ValidParents, SheepBreedRequestDTO> {

    @Override
    public boolean isValid(
            SheepBreedRequestDTO dto,
            ConstraintValidatorContext context
    ) {
        Integer p1 = dto.getParent1Id();
        Integer p2 = dto.getParent2Id();

        context.disableDefaultConstraintViolation();

        // Case 1: 0 or 1 provided → invalid
        if (p1 == null || p2 == null) {
            context.buildConstraintViolationWithTemplate(
                            "Both parent1Id and parent2Id must be provided"
                    ).addPropertyNode(p1 == null ? "parent1Id" : "parent2Id")
                    .addConstraintViolation();
            return false;
        }

        // Case 2: same ID → invalid
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

