package com.docgen.field;

import java.util.Map;
import java.util.Optional;

/**
 * Strategy for server-side validation of a single field's submitted value.
 *
 * <p>Implemented once per {@link FieldType} and registered automatically in
 * {@link FieldValidatorRegistry}. This is the backend half of the field-registry / strategy
 * pattern that keeps the platform extensible.
 */
public interface FieldValidator {

    /** The field type this validator handles. */
    FieldType type();

    /**
     * Validate a submitted value against the field definition.
     *
     * @param value    the submitted value (may be {@code null})
     * @param required whether the field is required
     * @param config   the field's {@code configJson} as a map (validation rules, options, etc.)
     * @return an error message if invalid, or empty if valid
     */
    Optional<String> validate(Object value, boolean required, Map<String, Object> config);

    /** Default required-check usable by implementations. */
    default Optional<String> checkRequired(Object value, boolean required) {
        boolean empty = value == null || (value instanceof String s && s.isBlank());
        if (required && empty) {
            return Optional.of("This field is required");
        }
        return Optional.empty();
    }
}
