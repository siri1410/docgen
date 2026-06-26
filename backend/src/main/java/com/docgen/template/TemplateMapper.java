package com.docgen.template;

import com.docgen.common.Json;
import com.docgen.template.TemplateDtos.FieldResponse;
import com.docgen.template.TemplateDtos.OptionDto;
import com.docgen.template.TemplateDtos.TemplateResponse;
import com.docgen.template.TemplateDtos.TemplateSummary;
import com.docgen.template.TemplateDtos.VersionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Maps template/field entities to their DTO representations (JSONB strings -> JsonNode).
 */
final class TemplateMapper {

    private TemplateMapper() {}

    static JsonNode node(String json) {
        return json == null ? null : Json.read(json);
    }

    static TemplateResponse toResponse(FormTemplate t, List<FormField> fields) {
        return new TemplateResponse(
                t.getId(), t.getName(), t.getSlug(), t.getDescription(), t.getCategory(),
                t.getStatus(), t.getCurrentVersion(),
                node(t.getSchemaJson()), node(t.getLayoutJson()),
                node(t.getValidationJson()), node(t.getRoleAccessJson()),
                fields.stream().map(TemplateMapper::toFieldResponse).toList(),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    static TemplateSummary toSummary(FormTemplate t) {
        return new TemplateSummary(t.getId(), t.getName(), t.getSlug(), t.getCategory(),
                t.getStatus(), t.getCurrentVersion(), t.getUpdatedAt());
    }

    static FieldResponse toFieldResponse(FormField f) {
        List<OptionDto> opts = f.getOptions().stream()
                .map(o -> new OptionDto(o.getValue(), o.getLabel()))
                .toList();
        return new FieldResponse(
                f.getId(), f.getFieldKey(), f.getLabel(), f.getType(),
                f.isRequired(), f.getType().isSensitive(), f.getOrderIndex(),
                node(f.getConfigJson()), opts);
    }

    static VersionResponse toVersionResponse(FormTemplateVersion v) {
        return new VersionResponse(v.getId(), v.getVersionNumber(), v.getPublishedBy(),
                v.getNotes(), v.getCreatedAt());
    }
}
