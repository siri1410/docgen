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
  // Field was filled from a fetched record and is locked (read-only). Renders a lock badge.
  locked?: boolean;
}

// Renders one field (label + control + help text) via the field registry,
// honoring conditional visibility. Shared by Preview and the live form.
export function FieldRenderer({ field, value, values, onChange, disabled, locked }: Props) {
  if (!isVisible(field, values)) return null;
  const descriptor = descriptorFor(field.type);
  const help = (field.config?.helpText ?? field.config?.help) as string | undefined;

  if (descriptor.presentational) {
    return <>{descriptor.render({ field, value, onChange, disabled })}</>;
  }

  return (
    <div className={`df-field${locked ? ' df-locked' : ''}`}>
      <label className="df-label">
        {field.label}
        {field.required && <span className="df-required">*</span>}
        {field.sensitive && <span className="df-badge" style={{ marginLeft: 8 }}>sensitive</span>}
        {locked && <span className="df-badge lock" style={{ marginLeft: 8 }}>🔒 from record</span>}
      </label>
      {descriptor.render({ field, value, onChange, disabled: disabled || locked })}
      {help && <div className="df-muted">{help}</div>}
    </div>
  );
}
