package com.docgen.template;

import com.docgen.field.FieldType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request/response DTOs for templates and fields. JSONB columns are exposed as {@link JsonNode}
 * so clients work with real JSON, not escaped strings.
 */
public final class TemplateDtos {

    private TemplateDtos() {}

    public record TemplateRequest(
            @NotBlank String name,
            String slug,
            String description,
            String category,
            JsonNode schema,
            JsonNode layout,
            JsonNode validation,
            JsonNode roleAccess) {}

    public record TemplateResponse(
            UUID id,
            String name,
            String slug,
            String description,
            String category,
            TemplateStatus status,
            int currentVersion,
            JsonNode schema,
            JsonNode layout,
            JsonNode validation,
            JsonNode roleAccess,
            List<FieldResponse> fields,
            Instant createdAt,
            Instant updatedAt) {}

    public record TemplateSummary(
            UUID id,
            String name,
            String slug,
            String category,
            TemplateStatus status,
            int currentVersion,
            Instant updatedAt) {}

    public record OptionDto(String value, String label) {}

    public record FieldRequest(
            String fieldKey,
            @NotBlank String label,
            @NotNull FieldType type,
            boolean required,
            Integer orderIndex,
            JsonNode config,
            List<OptionDto> options) {}

    public record FieldResponse(
            UUID id,
            String fieldKey,
            String label,
            FieldType type,
            boolean required,
            boolean sensitive,
            int orderIndex,
            JsonNode config,
            List<OptionDto> options) {}

    public record VersionResponse(
            UUID id,
            int versionNumber,
            String publishedBy,
            String notes,
            Instant createdAt) {}

    public record CloneRequest(String name) {}

    public record PublishRequest(String notes) {}
}
