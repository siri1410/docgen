package com.docgen.field;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Central lookup of {@link FieldValidator} strategies, keyed by {@link FieldType}.
 *
 * <p>Spring injects every {@code FieldValidator} bean; the registry builds the map at startup.
 * Types without a dedicated validator fall back to a default that performs only the required-check
 * (e.g. SECTION_HEADER, SIGNATURE, FILE_UPLOAD, ADDRESS, REPEATING_GROUP, HIDDEN, CALCULATED).
 */
@Component
public class FieldValidatorRegistry {

    private final Map<FieldType, FieldValidator> byType;

    /** Fallback validator: required-check only. */
    private static final class DefaultValidator implements FieldValidator {
        private final FieldType type;

        DefaultValidator(FieldType type) {
            this.type = type;
        }

        @Override public FieldType type() { return type; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            return checkRequired(value, required);
        }
    }

    public FieldValidatorRegistry(List<FieldValidator> validators) {
        this.byType = validators.stream()
                .collect(Collectors.toMap(FieldValidator::type, Function.identity(), (a, b) -> a));
    }

    public FieldValidator forType(FieldType type) {
        return byType.getOrDefault(type, new DefaultValidator(type));
    }

    /** Validate a value for a field type; returns an error message or empty if valid. */
    public Optional<String> validate(FieldType type, Object value, boolean required, Map<String, Object> config) {
        if (type.isPresentational()) {
            return Optional.empty();
        }
        return forType(type).validate(value, required, config);
    }
}
