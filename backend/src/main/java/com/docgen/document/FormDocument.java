package com.docgen.document;

import java.util.List;

/**
 * Format-neutral model of a rendered form document. The {@link DocumentModelBuilder} produces this
 * from a submission + its template; each {@code DocumentGenerator} renders it into a concrete format
 * (PDF, DOCX, RTF). Keeping rendering format-agnostic means a new output format is one new generator.
 */
public record FormDocument(
        String department,
        String logo,
        String title,
        String subtitle,
        String formId,
        int version,
        String submittedBy,
        String submittedAt,
        List<Block> blocks) {

    /** A document block: either a section heading or a label/value field row. */
    public record Block(boolean section, String label, String value, boolean sensitive) {
        public static Block section(String title) { return new Block(true, title, null, false); }
        public static Block field(String label, String value, boolean sensitive) {
            return new Block(false, label, value, sensitive);
        }
    }
}
