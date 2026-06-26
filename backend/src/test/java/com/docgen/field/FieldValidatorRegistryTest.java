package com.docgen.field;

import static org.assertj.core.api.Assertions.assertThat;

import com.docgen.field.Validators.CheckboxValidator;
import com.docgen.field.Validators.DropdownValidator;
import com.docgen.field.Validators.EmailValidator;
import com.docgen.field.Validators.NumberValidator;
import com.docgen.field.Validators.SsnValidator;
import com.docgen.field.Validators.TextValidator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FieldValidatorRegistryTest {

    private final FieldValidatorRegistry registry = new FieldValidatorRegistry(List.of(
            new TextValidator(), new NumberValidator(), new EmailValidator(),
            new SsnValidator(), new DropdownValidator(), new CheckboxValidator()));

    @Test
    void requiredFieldsRejectEmptyValues() {
        assertThat(registry.validate(FieldType.TEXT, null, true, Map.of())).isPresent();
        assertThat(registry.validate(FieldType.TEXT, "x", true, Map.of())).isEmpty();
    }

    @Test
    void emailFormatEnforced() {
        assertThat(registry.validate(FieldType.EMAIL, "bad", false, Map.of())).isPresent();
        assertThat(registry.validate(FieldType.EMAIL, "a@b.com", false, Map.of())).isEmpty();
    }

    @Test
    void ssnFormatEnforced() {
        assertThat(registry.validate(FieldType.SSN, "123", false, Map.of())).isPresent();
        assertThat(registry.validate(FieldType.SSN, "123-45-6789", false, Map.of())).isEmpty();
    }

    @Test
    void numberBoundsEnforced() {
        Map<String, Object> cfg = Map.of("min", 1, "max", 10);
        assertThat(registry.validate(FieldType.NUMBER, "0", false, cfg)).isPresent();
        assertThat(registry.validate(FieldType.NUMBER, "5", false, cfg)).isEmpty();
        assertThat(registry.validate(FieldType.NUMBER, "abc", false, cfg)).isPresent();
    }

    @Test
    void dropdownChecksAllowedOptions() {
        Map<String, Object> cfg = Map.of("options",
                List.of(Map.of("value", "NC"), Map.of("value", "SC")));
        assertThat(registry.validate(FieldType.DROPDOWN, "XX", false, cfg)).isPresent();
        assertThat(registry.validate(FieldType.DROPDOWN, "NC", false, cfg)).isEmpty();
    }

    @Test
    void presentationalTypesAlwaysValid() {
        assertThat(registry.validate(FieldType.SECTION_HEADER, null, true, Map.of())).isEmpty();
    }

    @Test
    void missingValidatorFallsBackToRequiredCheck() {
        // SIGNATURE has no dedicated validator -> default required-check only.
        assertThat(registry.validate(FieldType.SIGNATURE, null, true, Map.of())).isPresent();
        assertThat(registry.validate(FieldType.SIGNATURE, "sig-data", true, Map.of())).isEmpty();
    }
}
