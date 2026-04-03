package com.example.zorvyn.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, RecordFilterRequest> {

    @Override
    public boolean isValid(RecordFilterRequest req, ConstraintValidatorContext ctx) {
        if (req == null) {
            return true;
        }
        if (req.getStartDate() == null || req.getEndDate() == null) {
            return true;
        }
        return !req.getEndDate().isBefore(req.getStartDate());
    }
}

