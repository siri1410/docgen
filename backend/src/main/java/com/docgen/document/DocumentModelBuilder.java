package com.docgen.document;

import com.docgen.submission.SubmissionDtos.SubmissionResponse;
import com.docgen.submission.SubmissionService;
import com.docgen.template.TemplateDtos.FieldResponse;
import com.docgen.template.TemplateDtos.OptionDto;
import com.docgen.template.TemplateDtos.TemplateResponse;
import com.docgen.template.TemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Builds the format-neutral {@link FormDocument} for a submission: pulls branding from the template
 * schema, walks the fields in order, and resolves each stored value into a human-readable string
 * (option codes → labels, checkbox lists → joined labels). Sensitive values arrive already masked
 * from {@link SubmissionService}.
 */
@Component
public class DocumentModelBuilder {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'");

    private final TemplateService templates;
    private final SubmissionService submissions;

    public DocumentModelBuilder(TemplateService templates, SubmissionService submissions) {
        this.templates = templates;
        this.submissions = submissions;
    }

    public FormDocument build(UUID submissionId) {
        SubmissionResponse sub = submissions.get(submissionId);
        TemplateResponse tpl = templates.load(sub.templateId());
        JsonNode schema = tpl.schema();

        String department = text(schema, "department", text(schema, "agency", "Forms Service"));
        String logo = text(schema, "logo", "🏛");
        String title = text(schema, "title", tpl.name());

        List<FormDocument.Block> blocks = new ArrayList<>();
        for (FieldResponse f : tpl.fields()) {
            if (f.type().name().equals("SECTION_HEADER")) {
                blocks.add(FormDocument.Block.section(f.label()));
                continue;
            }
            if (f.type().name().equals("HIDDEN")) {
                continue;
            }
            String value = display(f, sub.values().get(f.fieldKey()));
            blocks.add(FormDocument.Block.field(f.label(), value, f.sensitive()));
        }

        String submittedAt = sub.createdAt() == null ? "" : STAMP.format(sub.createdAt().atZone(ZoneOffset.UTC));
        return new FormDocument(department, logo, title, tpl.description(),
                tpl.slug(), sub.templateVersion(), sub.submittedBy(), submittedAt, blocks);
    }

    /** Resolve a raw stored value to a display string, mapping option codes to their labels. */
    private static String display(FieldResponse f, Object raw) {
        if (raw == null) {
            return "—";
        }
        if (f.type().isOptionBacked() && f.options() != null) {
            Map<String, String> labels = new LinkedHashMap<>();
            for (OptionDto o : f.options()) {
                labels.put(o.value(), o.label());
            }
            if (raw instanceof List<?> list) { // CHECKBOX: list of selected codes
                List<String> mapped = new ArrayList<>();
                for (Object item : list) {
                    mapped.add(labels.getOrDefault(String.valueOf(item), String.valueOf(item)));
                }
                return mapped.isEmpty() ? "—" : String.join(", ", mapped);
            }
            return labels.getOrDefault(String.valueOf(raw), String.valueOf(raw));
        }
        if (raw instanceof Map<?, ?> || raw instanceof List<?>) {
            return raw.toString();
        }
        return String.valueOf(raw);
    }

    private static String text(JsonNode node, String field, String fallback) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return fallback;
        }
        return node.get(field).asText();
    }
}
