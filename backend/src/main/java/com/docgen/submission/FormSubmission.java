package com.docgen.submission;

import com.docgen.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A submitted instance of a form template. Individual values live in {@link FormSubmissionValue};
 * sensitive values are encrypted at rest.
 */
@Entity
@Table(name = "form_submission")
public class FormSubmission extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "template_version")
    private int templateVersion;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FormSubmissionValue> values = new ArrayList<>();

    protected FormSubmission() {
    }

    public FormSubmission(UUID templateId, UUID organizationId, String submittedBy, int templateVersion) {
        this.templateId = templateId;
        this.organizationId = organizationId;
        this.submittedBy = submittedBy;
        this.templateVersion = templateVersion;
    }

    public UUID getTemplateId() { return templateId; }
    public UUID getOrganizationId() { return organizationId; }
    public String getSubmittedBy() { return submittedBy; }
    public int getTemplateVersion() { return templateVersion; }

    public List<FormSubmissionValue> getValues() { return values; }

    public void addValue(FormSubmissionValue value) {
        value.setSubmission(this);
        this.values.add(value);
    }
}
