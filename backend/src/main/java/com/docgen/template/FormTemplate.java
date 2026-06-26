package com.docgen.template;

import com.docgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

/**
 * A reusable, versioned form/document template. The structural definition lives in extensible
 * JSONB columns ({@code schemaJson}, {@code layoutJson}, {@code validationJson}, {@code roleAccessJson})
 * so new capabilities can be added without schema migrations.
 */
@Entity
@Table(name = "form_template")
public class FormTemplate extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String slug;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateStatus status = TemplateStatus.DRAFT;

    /** Category/kind, e.g. INTAKE, MEMBER_REGISTRATION, ELIGIBILITY, CLAIMS, CASE_MGMT, ADDRESS_VERIFICATION, CUSTOM. */
    @Column(name = "category")
    private String category;

    @Column(name = "current_version", nullable = false)
    private int currentVersion = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", columnDefinition = "jsonb")
    private String schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_json", columnDefinition = "jsonb")
    private String layoutJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_json", columnDefinition = "jsonb")
    private String validationJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "role_access_json", columnDefinition = "jsonb")
    private String roleAccessJson;

    protected FormTemplate() {
    }

    public FormTemplate(UUID organizationId, String name, String slug) {
        this.organizationId = organizationId;
        this.name = name;
        this.slug = slug;
    }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TemplateStatus getStatus() { return status; }
    public void setStatus(TemplateStatus status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(int currentVersion) { this.currentVersion = currentVersion; }

    public String getSchemaJson() { return schemaJson; }
    public void setSchemaJson(String schemaJson) { this.schemaJson = schemaJson; }

    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String layoutJson) { this.layoutJson = layoutJson; }

    public String getValidationJson() { return validationJson; }
    public void setValidationJson(String validationJson) { this.validationJson = validationJson; }

    public String getRoleAccessJson() { return roleAccessJson; }
    public void setRoleAccessJson(String roleAccessJson) { this.roleAccessJson = roleAccessJson; }
}
