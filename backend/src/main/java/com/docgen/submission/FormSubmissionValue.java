package com.docgen.submission;

import com.docgen.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single submitted field value. Stored as a JSON-encoded string in {@code valueJson};
 * sensitive values are encrypted (then {@code encrypted=true}).
 */
@Entity
@Table(name = "form_submission_value")
public class FormSubmissionValue extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private FormSubmission submission;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "value_json", columnDefinition = "text")
    private String valueJson;

    @Column(nullable = false)
    private boolean sensitive = false;

    @Column(nullable = false)
    private boolean encrypted = false;

    protected FormSubmissionValue() {
    }

    public FormSubmissionValue(String fieldKey, String valueJson, boolean sensitive, boolean encrypted) {
        this.fieldKey = fieldKey;
        this.valueJson = valueJson;
        this.sensitive = sensitive;
        this.encrypted = encrypted;
    }

    public FormSubmission getSubmission() { return submission; }
    public void setSubmission(FormSubmission submission) { this.submission = submission; }

    public String getFieldKey() { return fieldKey; }
    public String getValueJson() { return valueJson; }
    public void setValueJson(String valueJson) { this.valueJson = valueJson; }

    public boolean isSensitive() { return sensitive; }
    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }
}
