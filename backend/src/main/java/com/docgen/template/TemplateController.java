package com.docgen.template;

import com.docgen.template.TemplateDtos.CloneRequest;
import com.docgen.template.TemplateDtos.FieldRequest;
import com.docgen.template.TemplateDtos.FieldResponse;
import com.docgen.template.TemplateDtos.PublishRequest;
import com.docgen.template.TemplateDtos.TemplateRequest;
import com.docgen.template.TemplateDtos.TemplateResponse;
import com.docgen.template.TemplateDtos.TemplateSummary;
import com.docgen.template.TemplateDtos.VersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for template + field management.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Templates", description = "Form template and field management")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @Operation(summary = "Create a template")
    @PostMapping("/templates")
    public TemplateResponse create(@Valid @RequestBody TemplateRequest req) {
        return service.create(req);
    }

    @Operation(summary = "List templates for the current organization")
    @GetMapping("/templates")
    public List<TemplateSummary> list() {
        return service.list();
    }

    @Operation(summary = "Get a template with its fields")
    @GetMapping("/templates/{id}")
    public TemplateResponse get(@PathVariable UUID id) {
        return service.load(id);
    }

    @Operation(summary = "Update a template")
    @PutMapping("/templates/{id}")
    public TemplateResponse update(@PathVariable UUID id, @Valid @RequestBody TemplateRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Publish a template (snapshots a new version)")
    @PostMapping("/templates/{id}/publish")
    public VersionResponse publish(@PathVariable UUID id, @RequestBody(required = false) PublishRequest req) {
        return service.publish(id, req == null ? null : req.notes());
    }

    @Operation(summary = "Clone a template into a new draft")
    @PostMapping("/templates/{id}/clone")
    public TemplateResponse clone(@PathVariable UUID id, @RequestBody(required = false) CloneRequest req) {
        return service.clone(id, req == null ? null : req.name());
    }

    @Operation(summary = "List a template's published versions")
    @GetMapping("/templates/{id}/versions")
    public List<VersionResponse> versions(@PathVariable UUID id) {
        return service.versions(id);
    }

    @Operation(summary = "Add a field to a template")
    @PostMapping("/templates/{templateId}/fields")
    public FieldResponse addField(@PathVariable UUID templateId, @Valid @RequestBody FieldRequest req) {
        return service.addField(templateId, req);
    }

    @Operation(summary = "Update a field")
    @PutMapping("/templates/{templateId}/fields/{fieldId}")
    public FieldResponse updateField(@PathVariable UUID templateId, @PathVariable UUID fieldId,
                                     @Valid @RequestBody FieldRequest req) {
        return service.updateField(templateId, fieldId, req);
    }

    @Operation(summary = "Delete a field")
    @DeleteMapping("/templates/{templateId}/fields/{fieldId}")
    public void deleteField(@PathVariable UUID templateId, @PathVariable UUID fieldId) {
        service.deleteField(templateId, fieldId);
    }
}
