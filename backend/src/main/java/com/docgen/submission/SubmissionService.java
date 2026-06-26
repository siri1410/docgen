package com.docgen.submission;

import com.docgen.audit.AuditService;
import com.docgen.common.Exceptions.BadRequestException;
import com.docgen.common.Exceptions.NotFoundException;
import com.docgen.common.Json;
import com.docgen.field.FieldValidatorRegistry;
import com.docgen.security.CryptoService;
import com.docgen.security.MaskingService;
import com.docgen.submission.SubmissionDtos.SubmissionResponse;
import com.docgen.submission.SubmissionDtos.SubmissionSummary;
import com.docgen.template.FormField;
import com.docgen.template.TemplateRepositories.FormFieldRepository;
import com.docgen.template.TemplateRepositories.FormTemplateRepository;
import com.docgen.user.CurrentTenant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists and reads form submissions, running server-side validation via the field-validator
 * strategy registry, encrypting sensitive values at rest, and masking them on read.
 */
@Service
public class SubmissionService {

    private final SubmissionRepositories submissions;
    private final FormFieldRepository fields;
    private final FormTemplateRepository templates;
    private final FieldValidatorRegistry validators;
    private final CryptoService crypto;
    private final MaskingService masking;
    private final CurrentTenant currentTenant;
    private final AuditService audit;

    public SubmissionService(SubmissionRepositories submissions, FormFieldRepository fields,
                            FormTemplateRepository templates, FieldValidatorRegistry validators,
                            CryptoService crypto, MaskingService masking,
                            CurrentTenant currentTenant, AuditService audit) {
        this.submissions = submissions;
        this.fields = fields;
        this.templates = templates;
        this.validators = validators;
        this.crypto = crypto;
        this.masking = masking;
        this.currentTenant = currentTenant;
        this.audit = audit;
    }

    @Transactional
    public SubmissionResponse submit(UUID templateId, Map<String, Object> values) {
        var template = templates.findById(templateId)
                .orElseThrow(() -> NotFoundException.of("FormTemplate", templateId));
        List<FormField> templateFields = fields.findByTemplateIdOrderByOrderIndexAsc(templateId);
        Map<String, Object> safeValues = values == null ? Map.of() : values;

        // Server-side validation across all fields via the strategy registry.
        List<String> errors = new ArrayList<>();
        for (FormField f : templateFields) {
            if (f.getType().isPresentational()) {
                continue;
            }
            Object value = safeValues.get(f.getFieldKey());
            Map<String, Object> config = Json.readMap(f.getConfigJson());
            Optional<String> error = validators.validate(f.getType(), value, f.isRequired(), config);
            error.ifPresent(msg -> errors.add(f.getFieldKey() + ": " + msg));
        }
        if (!errors.isEmpty()) {
            throw new BadRequestException("Validation failed: " + String.join("; ", errors));
        }

        FormSubmission submission = new FormSubmission(
                templateId, template.getOrganizationId(), currentTenant.currentActor(), template.getCurrentVersion());

        for (FormField f : templateFields) {
            if (f.getType().isPresentational() || !safeValues.containsKey(f.getFieldKey())) {
                continue;
            }
            boolean sensitive = f.getType().isSensitive();
            String json = Json.write(safeValues.get(f.getFieldKey()));
            String stored = sensitive ? crypto.encrypt(json) : json;
            submission.addValue(new FormSubmissionValue(f.getFieldKey(), stored, sensitive, sensitive));
        }

        submissions.save(submission);
        audit.record("FORM_SUBMIT", "FormSubmission", submission.getId(),
                Map.of("template", templateId.toString(), "fields", submission.getValues().size()));
        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionSummary> listForTemplate(UUID templateId) {
        return submissions.findByTemplateIdOrderByCreatedAtDesc(templateId).stream()
                .map(s -> new SubmissionSummary(s.getId(), s.getTemplateId(), s.getSubmittedBy(),
                        s.getValues().size(), s.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse get(UUID id) {
        return toResponse(submissions.findById(id)
                .orElseThrow(() -> NotFoundException.of("FormSubmission", id)));
    }

    private SubmissionResponse toResponse(FormSubmission s) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (FormSubmissionValue v : s.getValues()) {
            String json = v.isEncrypted() ? crypto.decrypt(v.getValueJson()) : v.getValueJson();
            Object value = Json.read(json) == null ? null : Json.mapper().convertValue(Json.read(json), Object.class);
            if (v.isSensitive() && value instanceof String str) {
                value = masking.maskUnlessAuthorized(str);
            }
            out.put(v.getFieldKey(), value);
        }
        return new SubmissionResponse(s.getId(), s.getTemplateId(), s.getTemplateVersion(),
                s.getSubmittedBy(), out, s.getCreatedAt());
    }
}
