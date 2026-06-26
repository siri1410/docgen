package com.docgen.template;

import com.docgen.common.BaseEntity;
import com.docgen.field.FieldType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A single field within a template. Behaviour (placeholder, masking, validation rules,
 * conditional visibility, calculated expression, etc.) is carried in the extensible
 * {@code configJson} JSONB column and interpreted by the field registry / renderer.
 */
@Entity
@Table(name = "form_field")
public class FormField extends BaseEntity {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType type;

    @Column(nullable = false)
    private boolean required = false;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_json", columnDefinition = "jsonb")
    private String configJson;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<FieldOption> options = new ArrayList<>();

    protected FormField() {
    }

    public FormField(UUID templateId, String fieldKey, String label, FieldType type) {
        this.templateId = templateId;
        this.fieldKey = fieldKey;
        this.label = label;
        this.type = type;
    }

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public List<FieldOption> getOptions() { return options; }

    public void setOptions(List<FieldOption> options) {
        this.options.clear();
        if (options != null) {
            options.forEach(this::addOption);
        }
    }

    public void addOption(FieldOption option) {
        option.setField(this);
        this.options.add(option);
    }
}
