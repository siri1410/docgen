package com.docgen.field;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the catalogue of supported field types and their metadata so the frontend field
 * registry can stay in sync with the backend (a single source of truth for extensibility).
 */
@RestController
@RequestMapping("/api/field-types")
@Tag(name = "Field Types", description = "Catalogue of supported field types")
public class FieldMetadataController {

    public record FieldTypeInfo(String type, boolean sensitive, boolean optionBacked, boolean presentational) {}

    @Operation(summary = "List supported field types and their metadata")
    @GetMapping
    public List<FieldTypeInfo> list() {
        return Arrays.stream(FieldType.values())
                .map(t -> new FieldTypeInfo(t.name(), t.isSensitive(), t.isOptionBacked(), t.isPresentational()))
                .toList();
    }
}
