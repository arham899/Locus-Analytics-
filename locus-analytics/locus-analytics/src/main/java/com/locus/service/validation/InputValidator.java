package com.locus.service.validation;

import com.locus.exception.ValidationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralized input validation utility used by all service implementations.
 *
 * <p>Collects field-level errors and throws a single {@link ValidationException}
 * containing all violations. This enables the UI to display per-field error messages.</p>
 *
 * @author Arham Manzoor (24i-0640)
 */
public final class InputValidator {

    private static final Set<String> VALID_CITIES = Set.of("Karachi", "Islamabad", "Lahore");
    private static final Set<String> VALID_PROPERTY_TYPES = Set.of("house", "apartment", "plot", "commercial");

    private final Map<String, String> errors = new HashMap<>();

    /**
     * Validates that a city is one of the supported values.
     */
    public InputValidator validateCity(String city) {
        if (city == null || city.isBlank()) {
            errors.put("city", "City is required");
        } else if (!VALID_CITIES.contains(city)) {
            errors.put("city", "City must be Karachi, Islamabad, or Lahore");
        }
        return this;
    }

    /**
     * Validates that a property type is one of the supported values.
     */
    public InputValidator validatePropertyType(String type) {
        if (type == null || type.isBlank()) {
            errors.put("propertyType", "Property type is required");
        } else if (!VALID_PROPERTY_TYPES.contains(type.toLowerCase())) {
            errors.put("propertyType", "Property type must be house, apartment, plot, or commercial");
        }
        return this;
    }

    /**
     * Validates that a numeric value is strictly positive (> 0).
     */
    public InputValidator validatePositive(String field, double value) {
        if (value <= 0) {
            errors.put(field, field + " must be greater than 0");
        }
        return this;
    }

    /**
     * Validates that a numeric value is non-negative (>= 0).
     */
    public InputValidator validateNonNegative(String field, double value) {
        if (value < 0) {
            errors.put(field, field + " must be non-negative");
        }
        return this;
    }

    /**
     * Validates that an object is not null.
     */
    public InputValidator validateNotNull(String field, Object value) {
        if (value == null) {
            errors.put(field, field + " is required");
        }
        return this;
    }

    /**
     * Validates that a string is not null or blank.
     */
    public InputValidator validateNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            errors.put(field, field + " is required");
        }
        return this;
    }

    /**
     * Validates that min <= max for a numeric range.
     */
    public InputValidator validateRange(String field, Double min, Double max) {
        if (min != null && max != null && min > max) {
            errors.put(field, "Minimum " + field + " cannot exceed maximum");
        }
        return this;
    }

    /**
     * Validates that a date is not in the future.
     */
    public InputValidator validateNotFuture(String field, LocalDate date) {
        if (date != null && date.isAfter(LocalDate.now())) {
            errors.put(field, field + " cannot be in the future");
        }
        return this;
    }

    /**
     * Validates list size is within bounds.
     */
    public InputValidator validateListSize(String field, List<?> list, int min, int max) {
        if (list == null || list.size() < min) {
            errors.put(field, "At least " + min + " " + field + " required");
        } else if (list.size() > max) {
            errors.put(field, "At most " + max + " " + field + " allowed");
        }
        return this;
    }

    /**
     * Throws ValidationException if any errors were collected.
     * Call this after all validate* calls.
     */
    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    /**
     * Returns true if there are validation errors.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the collected errors map.
     */
    public Map<String, String> getErrors() {
        return errors;
    }
}
