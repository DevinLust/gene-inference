package com.progressengine.geneinference.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DistributionValidator.class)
public @interface ValidDistribution {
    String message() default "Invalid distribution";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
