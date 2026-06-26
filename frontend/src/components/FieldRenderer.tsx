import { descriptorFor } from '../fields/registry';
import type { FormField } from '../types';

// Evaluates a field's conditional-visibility rule against the current form values.
export function isVisible(field: FormField, values: Record<string, unknown>): boolean {
  const cond = field.config?.visibleWhen;
  if (!cond) return true;
  const v = values[cond.field];
  switch (cond.operator) {
    case 'eq': return v === cond.value;
    case 'neq': return v !== cond.value;
    case 'in': return Array.isArray(cond.value) && (cond.value as unknown[]).includes(v);
    case 'notEmpty': return v !== undefined && v !== null && v !== '';
    default: return true;
  }
}

interface Props {
  field: FormField;
  value: unknown;
  values: Record<string, unknown>;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}

// Renders one field (label + control + help text) via the field registry,
// honoring conditional visibility. Shared by Preview and the live form.
export function FieldRenderer({ field, value, values, onChange, disabled }: Props) {
  if (!isVisible(field, values)) return null;
  const descriptor = descriptorFor(field.type);

  if (descriptor.presentational) {
    return <>{descriptor.render({ field, value, onChange, disabled })}</>;
  }

  return (
    <div className="df-field">
      <label className="df-label">
        {field.label}
        {field.required && <span className="df-required">*</span>}
        {field.sensitive && <span className="df-badge" style={{ marginLeft: 8 }}>sensitive</span>}
      </label>
      {descriptor.render({ field, value, onChange, disabled })}
      {field.config?.helpText && <div className="df-muted">{field.config.helpText as string}</div>}
    </div>
  );
}
