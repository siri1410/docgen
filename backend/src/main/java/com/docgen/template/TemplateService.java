package com.docgen.template;

import com.docgen.audit.AuditService;
import com.docgen.common.Exceptions.BadRequestException;
import com.docgen.common.Exceptions.NotFoundException;
import com.docgen.common.Json;
import com.docgen.template.TemplateDtos.FieldRequest;
import com.docgen.template.TemplateDtos.TemplateRequest;
import com.docgen.template.TemplateDtos.TemplateResponse;
import com.docgen.template.TemplateDtos.TemplateSummary;
import com.docgen.template.TemplateDtos.VersionResponse;
import com.docgen.template.TemplateRepositories.FormFieldRepository;
import com.docgen.template.TemplateRepositories.FormTemplateRepository;
import com.docgen.template.TemplateRepositories.FormTemplateVersionRepository;
import com.docgen.user.CurrentTenant;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core template lifecycle: CRUD, field management, publish (snapshot versioning) and clone.
 */
@Service
public class TemplateService {

    private final FormTemplateRepository templates;
    private final FormFieldRepository fields;
    private final FormTemplateVersionRepository versions;
    private final CurrentTenant currentTenant;
    private final AuditService audit;

    public TemplateService(FormTemplateRepository templates, FormFieldRepository fields,
                          FormTemplateVersionRepository versions, CurrentTenant currentTenant,
                          AuditService audit) {
        this.templates = templates;
        this.fields = fields;
        this.versions = versions;
        this.currentTenant = currentTenant;
        this.audit = audit;
    }

    // ---- Templates ----

    @Transactional
    public TemplateResponse create(TemplateRequest req) {
        FormTemplate t = new FormTemplate(currentTenant.organizationId(), req.name(), slugify(req.name(), req.slug()));
        applyTemplateFields(t, req);
        templates.save(t);
        audit.record("TEMPLATE_CREATE", "FormTemplate", t.getId(), Map.of("name", t.getName()));
        return load(t.getId());
    }

    @Transactional(readOnly = true)
    public List<TemplateSummary> list() {
        return templates.findByOrganizationIdOrderByCreatedAtDesc(currentTenant.organizationId())
                .stream().map(TemplateMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public TemplateResponse load(UUID id) {
        FormTemplate t = require(id);
        return TemplateMapper.toResponse(t, fields.findByTemplateIdOrderByOrderIndexAsc(id));
    }

    @Transactional
    public TemplateResponse update(UUID id, TemplateRequest req) {
        FormTemplate t = require(id);
        applyTemplateFields(t, req);
        templates.save(t);
        audit.record("TEMPLATE_UPDATE", "FormTemplate", id, Map.of("name", t.getName()));
        return load(id);
    }

    @Transactional
    public VersionResponse publish(UUID id, String notes) {
        FormTemplate t = require(id);
        TemplateResponse snapshot = TemplateMapper.toResponse(t, fields.findByTemplateIdOrderByOrderIndexAsc(id));
        FormTemplateVersion version = new FormTemplateVersion(
                id, t.getCurrentVersion(), currentTenant.currentActor(), Json.write(snapshot));
        version.setNotes(notes);
        versions.save(version);

        t.setStatus(TemplateStatus.PUBLISHED);
        t.setCurrentVersion(t.getCurrentVersion() + 1);
        templates.save(t);
        audit.record("TEMPLATE_PUBLISH", "FormTemplate", id, Map.of("version", version.getVersionNumber()));
        return TemplateMapper.toVersionResponse(version);
    }

    @Transactional
    public TemplateResponse clone(UUID id, String newName) {
        FormTemplate src = require(id);
        String name = newName != null && !newName.isBlank() ? newName : src.getName() + " (copy)";
        FormTemplate copy = new FormTemplate(currentTenant.organizationId(), name, slugify(name, null));
        copy.setDescription(src.getDescription());
        copy.setCategory(src.getCategory());
        copy.setSchemaJson(src.getSchemaJson());
        copy.setLayoutJson(src.getLayoutJson());
        copy.setValidationJson(src.getValidationJson());
        copy.setRoleAccessJson(src.getRoleAccessJson());
        copy.setStatus(TemplateStatus.DRAFT);
        templates.save(copy);

        for (FormField f : fields.findByTemplateIdOrderByOrderIndexAsc(id)) {
            FormField nf = new FormField(copy.getId(), f.getFieldKey(), f.getLabel(), f.getType());
            nf.setRequired(f.isRequired());
            nf.setOrderIndex(f.getOrderIndex());
            nf.setConfigJson(f.getConfigJson());
            f.getOptions().forEach(o -> nf.addOption(new FieldOption(o.getValue(), o.getLabel(), o.getOrderIndex())));
            fields.save(nf);
        }
        audit.record("TEMPLATE_CLONE", "FormTemplate", copy.getId(), Map.of("source", id.toString()));
        return load(copy.getId());
    }

    @Transactional(readOnly = true)
    public List<VersionResponse> versions(UUID id) {
        require(id);
        return versions.findByTemplateIdOrderByVersionNumberDesc(id)
                .stream().map(TemplateMapper::toVersionResponse).toList();
    }

    // ---- Fields ----

    @Transactional
    public TemplateDtos.FieldResponse addField(UUID templateId, FieldRequest req) {
        require(templateId);
        FormField f = new FormField(templateId,
                req.fieldKey() != null ? req.fieldKey() : defaultKey(req.label()),
                req.label(), req.type());
        applyField(f, req);
        fields.save(f);
        audit.record("FIELD_ADD", "FormField", f.getId(), Map.of("template", templateId.toString()));
        return TemplateMapper.toFieldResponse(f);
    }

    @Transactional
    public TemplateDtos.FieldResponse updateField(UUID templateId, UUID fieldId, FieldRequest req) {
        FormField f = fields.findById(fieldId)
                .filter(x -> x.getTemplateId().equals(templateId))
                .orElseThrow(() -> NotFoundException.of("FormField", fieldId));
        f.setLabel(req.label());
        f.setType(req.type());
        applyField(f, req);
        fields.save(f);
        audit.record("FIELD_UPDATE", "FormField", fieldId, Map.of("template", templateId.toString()));
        return TemplateMapper.toFieldResponse(f);
    }

    @Transactional
    public void deleteField(UUID templateId, UUID fieldId) {
        FormField f = fields.findById(fieldId)
                .filter(x -> x.getTemplateId().equals(templateId))
                .orElseThrow(() -> NotFoundException.of("FormField", fieldId));
        fields.delete(f);
        audit.record("FIELD_DELETE", "FormField", fieldId, Map.of("template", templateId.toString()));
    }

    // ---- Internals ----

    private void applyTemplateFields(FormTemplate t, TemplateRequest req) {
        if (req.name() != null) t.setName(req.name());
        if (req.slug() != null && !req.slug().isBlank()) t.setSlug(req.slug());
        t.setDescription(req.description());
        t.setCategory(req.category());
        t.setSchemaJson(toJson(req.schema()));
        t.setLayoutJson(toJson(req.layout()));
        t.setValidationJson(toJson(req.validation()));
        t.setRoleAccessJson(toJson(req.roleAccess()));
    }

    private void applyField(FormField f, FieldRequest req) {
        f.setRequired(req.required());
        if (req.orderIndex() != null) f.setOrderIndex(req.orderIndex());
        f.setConfigJson(toJson(req.config()));
        if (req.options() != null) {
            AtomicInteger i = new AtomicInteger();
            List<FieldOption> opts = req.options().stream()
                    .map(o -> new FieldOption(o.value(), o.label(), i.getAndIncrement()))
                    .toList();
            f.setOptions(opts);
        }
    }

    private static String toJson(JsonNode node) {
        return node == null || node.isNull() ? null : Json.write(node);
    }

    private FormTemplate require(UUID id) {
        return templates.findById(id).orElseThrow(() -> NotFoundException.of("FormTemplate", id));
    }

    private static String slugify(String name, String explicit) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit;
        }
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Template name is required");
        }
        String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String defaultKey(String label) {
        return label == null ? "field" : label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("(^_|_$)", "");
    }
}
