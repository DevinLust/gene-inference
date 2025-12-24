package com.progressengine.geneinference.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenotypesValidator.class)
public @interface ValidGenotypes {
    String message() default "Invalid genotypes map";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
