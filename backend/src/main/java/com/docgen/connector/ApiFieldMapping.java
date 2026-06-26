package com.docgen.connector;

import com.docgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Maps a JSONPath expression in an API response to a target form field, with an optional
 * transform, fallback and required flag. This is the heart of the configurable prefill layer.
 *
 * <p>Example: {@code source="$.member.firstName", target="firstName", transform="capitalize", required=true}.
 */
@Entity
@Table(name = "api_field_mapping")
public class ApiFieldMapping extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    /** JSONPath into the connector response, e.g. {@code $.member.firstName}. */
    @Column(nullable = false)
    private String source;

    /** Target form field key, e.g. {@code firstName} or {@code address.city}. */
    @Column(nullable = false)
    private String target;

    /** Optional transform function name (see {@code TransformFunctions}). */
    @Column
    private String transform;

    @Column(name = "fallback_value")
    private String fallbackValue;

    @Column(nullable = false)
    private boolean required = false;

    protected ApiFieldMapping() {
    }

    public ApiFieldMapping(UUID templateId, UUID connectorId, String source, String target) {
        this.templateId = templateId;
        this.connectorId = connectorId;
        this.source = source;
        this.target = target;
    }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public UUID getConnectorId() { return connectorId; }
    public void setConnectorId(UUID connectorId) { this.connectorId = connectorId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getTransform() { return transform; }
    public void setTransform(String transform) { this.transform = transform; }

    public String getFallbackValue() { return fallbackValue; }
    public void setFallbackValue(String fallbackValue) { this.fallbackValue = fallbackValue; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}
