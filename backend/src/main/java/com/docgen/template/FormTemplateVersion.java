package com.docgen.template;

import com.docgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Immutable snapshot of a template (schema + layout + validation + fields + mappings) captured
 * at publish time. Enables version history and safe rollback.
 */
@Entity
@Table(name = "form_template_version")
public class FormTemplateVersion extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    /** Full self-contained snapshot of the template definition at this version. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_json", columnDefinition = "jsonb", nullable = false)
    private String snapshotJson;

    protected FormTemplateVersion() {
    }

    public FormTemplateVersion(UUID templateId, int versionNumber, String publishedBy, String snapshotJson) {
        this.templateId = templateId;
        this.versionNumber = versionNumber;
        this.publishedBy = publishedBy;
        this.snapshotJson = snapshotJson;
    }

    public UUID getTemplateId() { return templateId; }
    public int getVersionNumber() { return versionNumber; }
    public String getPublishedBy() { return publishedBy; }
    public String getSnapshotJson() { return snapshotJson; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
