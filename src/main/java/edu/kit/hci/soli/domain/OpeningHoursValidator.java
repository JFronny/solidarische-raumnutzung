package edu.kit.hci.soli.domain;

package edu.kit.hci.soli.domain;

import jakarta.validation.ConstraintValidator; // This import should work with the latest version of the Jakarta EE API
import jakarta.validation.ConstraintValidatorContext; // This import should work with the latest version of the Jakarta EE API

import java.time.LocalTime;

/**
 * Validator for the OpeningHours class.
 * Ensures that the opening time is before the closing time.
 */
public class OpeningHoursValidator implements ConstraintValidator<ValidOpeningHours, OpeningHours> {
    /**
     * Validates the OpeningHours object.
     *
     * @param openingHours the OpeningHours object to validate
     * @param context      the context in which the constraint is evaluated
     * @return true if the opening time is before the closing time, false otherwise
     */
    @Override
    public boolean isValid(OpeningHours openingHours, ConstraintValidatorContext context) {
        if (openingHours == null) {
            return true;
        }
        LocalTime openingTime = openingHours.getOpeningTime();
        LocalTime closingTime = openingHours.getClosingTime();
        return openingTime != null && closingTime != null && openingTime.isBefore(closingTime);
    }
}
