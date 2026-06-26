package com.docgen.template;

import com.docgen.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A selectable option for option-backed fields (dropdown, radio, checkbox).
 */
@Entity
@Table(name = "field_option")
public class FieldOption extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    @JsonIgnore
    private FormField field;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String label;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    protected FieldOption() {
    }

    public FieldOption(String value, String label, int orderIndex) {
        this.value = value;
        this.label = label;
        this.orderIndex = orderIndex;
    }

    public FormField getField() { return field; }
    public void setField(FormField field) { this.field = field; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
