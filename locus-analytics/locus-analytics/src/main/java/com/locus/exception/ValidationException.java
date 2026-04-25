package com.locus.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Custom exception for input validation failures across the service layer.
 *
 * <p>Carries a {@code Map<String, String>} of field-level error messages so that
 * the UI can display specific feedback per form field (e.g. "city" → "City is required").</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Map<String, String> errors = new HashMap<>();
 * errors.put("city", "City must be Karachi, Islamabad, or Lahore");
 * errors.put("price", "Price must be greater than 0");
 * throw new ValidationException("Invalid property input", errors);
 * }</pre>
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    /**
     * Creates a validation exception with a message and field-level errors.
     *
     * @param message     general error message
     * @param fieldErrors map of field name → error description
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = (fieldErrors != null) ? fieldErrors : Collections.emptyMap();
    }

    /**
     * Creates a validation exception with just a message (no field-level detail).
     *
     * @param message general error message
     */
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    /**
     * Returns the map of field-level validation errors.
     *
     * @return unmodifiable map of field name → error message
     */
    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    /**
     * Returns true if this exception contains field-level errors.
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "message='" + getMessage() + '\'' +
                ", fieldErrors=" + fieldErrors +
                '}';
    }
}
