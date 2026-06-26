package com.docgen.field;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Concrete {@link FieldValidator} strategies, one per type that needs real rules.
 * Types without a dedicated validator fall back to {@link FieldValidatorRegistry}'s default
 * (required-check only). Each is a Spring bean discovered by the registry.
 */
public final class Validators {

    private Validators() {}

    private static String str(Object value) {
        return value == null ? null : value.toString();
    }

    @Component
    public static class TextValidator implements FieldValidator {
        @Override public FieldType type() { return FieldType.TEXT; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            String s = str(value);
            Object min = config.get("minLength");
            Object max = config.get("maxLength");
            Object pattern = config.get("pattern");
            if (min instanceof Number n && s.length() < n.intValue()) {
                return Optional.of("Must be at least " + n.intValue() + " characters");
            }
            if (max instanceof Number n && s.length() > n.intValue()) {
                return Optional.of("Must be at most " + n.intValue() + " characters");
            }
            if (pattern instanceof String p && !p.isBlank() && !Pattern.compile(p).matcher(s).matches()) {
                return Optional.of("Does not match required format");
            }
            return Optional.empty();
        }
    }

    @Component
    public static class NumberValidator implements FieldValidator {
        @Override public FieldType type() { return FieldType.NUMBER; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            double num;
            try {
                num = Double.parseDouble(str(value));
            } catch (NumberFormatException e) {
                return Optional.of("Must be a number");
            }
            if (config.get("min") instanceof Number n && num < n.doubleValue()) {
                return Optional.of("Must be >= " + n);
            }
            if (config.get("max") instanceof Number n && num > n.doubleValue()) {
                return Optional.of("Must be <= " + n);
            }
            return Optional.empty();
        }
    }

    @Component
    public static class DateValidator implements FieldValidator {
        @Override public FieldType type() { return FieldType.DATE; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            try {
                LocalDate.parse(str(value));
            } catch (DateTimeParseException e) {
                return Optional.of("Must be an ISO date (YYYY-MM-DD)");
            }
            return Optional.empty();
        }
    }

    @Component
    public static class EmailValidator implements FieldValidator {
        private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        @Override public FieldType type() { return FieldType.EMAIL; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            return EMAIL.matcher(str(value)).matches() ? Optional.empty() : Optional.of("Invalid email address");
        }
    }

    @Component
    public static class PhoneValidator implements FieldValidator {
        private static final Pattern PHONE = Pattern.compile("^\\+?[0-9().\\-\\s]{7,20}$");
        @Override public FieldType type() { return FieldType.PHONE; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            return PHONE.matcher(str(value)).matches() ? Optional.empty() : Optional.of("Invalid phone number");
        }
    }

    @Component
    public static class SsnValidator implements FieldValidator {
        private static final Pattern SSN = Pattern.compile("^\\d{3}-?\\d{2}-?\\d{4}$");
        @Override public FieldType type() { return FieldType.SSN; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            return SSN.matcher(str(value)).matches()
                    ? Optional.empty()
                    : Optional.of("SSN must be 9 digits (e.g. 123-45-6789)");
        }
    }

    @Component
    public static class MpiValidator implements FieldValidator {
        @Override public FieldType type() { return FieldType.MPI; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            String s = str(value);
            return s.length() >= 3 ? Optional.empty() : Optional.of("Member ID is too short");
        }
    }

    /** Shared logic for option-backed types (dropdown/radio/checkbox). */
    abstract static class OptionValidator implements FieldValidator {
        @SuppressWarnings("unchecked")
        protected List<String> allowedValues(Map<String, Object> config) {
            Object opts = config.get("options");
            if (opts instanceof List<?> list) {
                return list.stream()
                        .map(o -> o instanceof Map<?, ?> m ? String.valueOf(m.get("value")) : String.valueOf(o))
                        .toList();
            }
            return List.of();
        }
    }

    @Component
    public static class DropdownValidator extends OptionValidator {
        @Override public FieldType type() { return FieldType.DROPDOWN; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            List<String> allowed = allowedValues(config);
            if (!allowed.isEmpty() && !allowed.contains(str(value))) {
                return Optional.of("Value is not one of the allowed options");
            }
            return Optional.empty();
        }
    }

    @Component
    public static class RadioValidator extends OptionValidator {
        @Override public FieldType type() { return FieldType.RADIO; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            Optional<String> req = checkRequired(value, required);
            if (req.isPresent() || value == null) {
                return req;
            }
            List<String> allowed = allowedValues(config);
            if (!allowed.isEmpty() && !allowed.contains(str(value))) {
                return Optional.of("Value is not one of the allowed options");
            }
            return Optional.empty();
        }
    }

    @Component
    public static class CheckboxValidator extends OptionValidator {
        @Override public FieldType type() { return FieldType.CHECKBOX; }

        @Override
        public Optional<String> validate(Object value, boolean required, Map<String, Object> config) {
            if (required && (value == null || (value instanceof List<?> l && l.isEmpty()))) {
                return Optional.of("Select at least one option");
            }
            if (value == null) {
                return Optional.empty();
            }
            List<String> allowed = allowedValues(config);
            if (!allowed.isEmpty() && value instanceof List<?> selected) {
                for (Object sel : selected) {
                    if (!allowed.contains(String.valueOf(sel))) {
                        return Optional.of("Value '" + sel + "' is not an allowed option");
                    }
                }
            }
            return Optional.empty();
        }
    }
}
