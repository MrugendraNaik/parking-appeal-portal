package com.vit.parkingappeal.service;

import java.util.Map;

/** Thrown when submitted data fails validation. Carries field -> message pairs. */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed: " + errors);
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
