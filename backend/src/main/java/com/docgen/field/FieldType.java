package com.docgen.field;

/**
 * Supported field types. Each carries metadata used by validation, masking and rendering.
 *
 * <p><b>Extension point:</b> adding a new field type means adding a value here and a matching
 * {@link FieldValidator} bean — no core logic changes.
 */
public enum FieldType {
    TEXT(false, false),
    NUMBER(false, false),
    DATE(false, false),
    DROPDOWN(false, true),
    CHECKBOX(false, true),
    RADIO(false, true),
    FILE_UPLOAD(false, false),
    ADDRESS(false, false),
    SSN(true, false),
    MPI(true, false),
    EMAIL(false, false),
    PHONE(false, false),
    SIGNATURE(false, false),
    CALCULATED(false, false),
    HIDDEN(false, false),
    SECTION_HEADER(false, false),
    REPEATING_GROUP(false, false);

    private final boolean sensitive;
    private final boolean optionBacked;

    FieldType(boolean sensitive, boolean optionBacked) {
        this.sensitive = sensitive;
        this.optionBacked = optionBacked;
    }

    /** Whether values of this type are masked in responses and encrypted at rest. */
    public boolean isSensitive() {
        return sensitive;
    }

    /** Whether this type draws from a set of {@code FieldOption}s (dropdown/radio/checkbox). */
    public boolean isOptionBacked() {
        return optionBacked;
    }

    /** Presentational-only types hold no submitted value. */
    public boolean isPresentational() {
        return this == SECTION_HEADER;
    }
}
