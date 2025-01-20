package edu.kit.hci.soli.domain;

import jakarta.validation.Constraint; // This import should work with the latest version of the Jakarta EE API
import jakarta.validation.Payload; // This import should work with the latest version of the Jakarta EE API

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for validating opening hours.
 * Ensures that the opening time is before the closing time.
 */
@Constraint(validatedBy = OpeningHoursValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOpeningHours {
    /**
     * The default error message when the opening hours are invalid.
     *
     * @return the error message
     */
    String message() default "Invalid opening hours";

    /**
     * Allows the specification of validation groups, to which this constraint belongs.
     *
     * @return the array of validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients of the Bean Validation API to assign custom payload objects to a constraint.
     *
     * @return the array of payload classes
     */
    Class<? extends Payload>[] payload() default {};
}