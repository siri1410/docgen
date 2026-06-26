import type { FieldConfig, FormField } from '../types';

type Condition = NonNullable<FieldConfig['visibleWhen']>;

interface Props {
  value?: Condition;
  otherFields: FormField[];
  onChange: (cond?: Condition) => void;
}

// Builds a single conditional-visibility rule: show this field only when another
// field matches a condition. Stored in the field's config.visibleWhen.
export function ConditionalLogicBuilder({ value, otherFields, onChange }: Props) {
  const enabled = !!value;
  return (
    <div>
      <label className="df-radio">
        <input
          type="checkbox"
          checked={enabled}
          onChange={(e) =>
            onChange(e.target.checked
              ? { field: otherFields[0]?.fieldKey ?? '', operator: 'eq', value: '' }
              : undefined)}
        />
        Conditional visibility
      </label>
      {enabled && value && (
        <div className="df-row" style={{ marginTop: 8 }}>
          <select className="df-input" style={{ flex: 1 }} value={value.field}
            onChange={(e) => onChange({ ...value, field: e.target.value })}>
            {otherFields.map((f) => <option key={f.id} value={f.fieldKey}>{f.label}</option>)}
          </select>
          <select className="df-input" style={{ width: 110 }} value={value.operator}
            onChange={(e) => onChange({ ...value, operator: e.target.value as Condition['operator'] })}>
            <option value="eq">equals</option>
            <option value="neq">not equals</option>
            <option value="notEmpty">is filled</option>
          </select>
          {value.operator !== 'notEmpty' && (
            <input className="df-input" style={{ width: 120 }} placeholder="value"
              value={(value.value as string) ?? ''}
              onChange={(e) => onChange({ ...value, value: e.target.value })} />
          )}
        </div>
      )}
    </div>
  );
}
