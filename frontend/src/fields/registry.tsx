import type { ReactNode } from 'react';
import type { FieldType, FormField } from '../types';

// ---------------------------------------------------------------------------
// Field registry — the frontend half of the extensibility model.
// Each field type has one descriptor: palette metadata, a default config, and a
// renderer used by both Preview and the canvas. Add a field type by adding one
// entry here; the palette, renderer and property panel all read from this map.
// ---------------------------------------------------------------------------

export interface RendererProps {
  field: FormField;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}

export interface FieldDescriptor {
  type: FieldType;
  label: string;
  icon: string;
  group: 'Basic' | 'Choice' | 'Identity' | 'Advanced' | 'Layout';
  optionBacked?: boolean;
  sensitive?: boolean;
  presentational?: boolean;
  defaultConfig?: Record<string, unknown>;
  render: (p: RendererProps) => ReactNode;
}

function TextLike(inputType: string) {
  return ({ field, value, onChange, disabled }: RendererProps) => (
    <input
      type={inputType}
      className="df-input"
      disabled={disabled}
      placeholder={(field.config?.placeholder as string) ?? ''}
      value={(value as string) ?? ''}
      onChange={(e) => onChange(e.target.value)}
    />
  );
}

const descriptors: FieldDescriptor[] = [
  { type: 'TEXT', label: 'Text', icon: '𝐓', group: 'Basic', render: TextLike('text') },
  { type: 'NUMBER', label: 'Number', icon: '#', group: 'Basic', render: TextLike('number') },
  { type: 'DATE', label: 'Date', icon: '📅', group: 'Basic', render: TextLike('date') },
  { type: 'EMAIL', label: 'Email', icon: '✉', group: 'Basic', render: TextLike('email') },
  { type: 'PHONE', label: 'Phone', icon: '☎', group: 'Basic', render: TextLike('tel') },
  {
    type: 'DROPDOWN', label: 'Dropdown', icon: '▾', group: 'Choice', optionBacked: true,
    render: ({ field, value, onChange, disabled }) => (
      <select className="df-input" disabled={disabled} value={(value as string) ?? ''}
        onChange={(e) => onChange(e.target.value)}>
        <option value="">— select —</option>
        {(field.options ?? []).map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    ),
  },
  {
    type: 'RADIO', label: 'Radio', icon: '◉', group: 'Choice', optionBacked: true,
    render: ({ field, value, onChange, disabled }) => (
      <div className="df-radio-group">
        {(field.options ?? []).map((o) => (
          <label key={o.value} className="df-radio">
            <input type="radio" disabled={disabled} name={field.fieldKey}
              checked={value === o.value} onChange={() => onChange(o.value)} />
            {o.label}
          </label>
        ))}
      </div>
    ),
  },
  {
    type: 'CHECKBOX', label: 'Checkbox', icon: '☑', group: 'Choice', optionBacked: true,
    render: ({ field, value, onChange, disabled }) => {
      const selected = Array.isArray(value) ? (value as string[]) : [];
      const toggle = (v: string) =>
        onChange(selected.includes(v) ? selected.filter((x) => x !== v) : [...selected, v]);
      return (
        <div className="df-radio-group">
          {(field.options ?? []).map((o) => (
            <label key={o.value} className="df-radio">
              <input type="checkbox" disabled={disabled}
                checked={selected.includes(o.value)} onChange={() => toggle(o.value)} />
              {o.label}
            </label>
          ))}
        </div>
      );
    },
  },
  {
    type: 'SSN', label: 'SSN', icon: '🔒', group: 'Identity', sensitive: true, defaultConfig: { mask: true },
    render: TextLike('text'),
  },
  {
    type: 'MPI', label: 'Member ID', icon: '🪪', group: 'Identity', sensitive: true, defaultConfig: { mask: true },
    render: TextLike('text'),
  },
  {
    type: 'ADDRESS', label: 'Address Block', icon: '🏠', group: 'Advanced',
    render: ({ value, onChange, disabled }) => {
      const v = (value as Record<string, string>) ?? {};
      const set = (k: string, val: string) => onChange({ ...v, [k]: val });
      return (
        <div className="df-address">
          <input className="df-input" placeholder="Line 1" disabled={disabled}
            value={v.line1 ?? ''} onChange={(e) => set('line1', e.target.value)} />
          <div className="df-row">
            <input className="df-input" placeholder="City" disabled={disabled}
              value={v.city ?? ''} onChange={(e) => set('city', e.target.value)} />
            <input className="df-input" placeholder="State" disabled={disabled}
              value={v.state ?? ''} onChange={(e) => set('state', e.target.value)} />
            <input className="df-input" placeholder="ZIP" disabled={disabled}
              value={v.zip ?? ''} onChange={(e) => set('zip', e.target.value)} />
          </div>
        </div>
      );
    },
  },
  {
    type: 'FILE_UPLOAD', label: 'File Upload', icon: '📎', group: 'Advanced',
    render: ({ onChange, disabled }) => (
      <input type="file" className="df-input" disabled={disabled}
        onChange={(e) => onChange(e.target.files?.[0]?.name ?? '')} />
    ),
  },
  {
    type: 'SIGNATURE', label: 'Signature', icon: '✍', group: 'Advanced',
    render: ({ value, onChange, disabled }) => (
      <input className="df-input" placeholder="Type full name to sign" disabled={disabled}
        value={(value as string) ?? ''} onChange={(e) => onChange(e.target.value)}
        style={{ fontFamily: 'cursive' }} />
    ),
  },
  {
    type: 'CALCULATED', label: 'Calculated', icon: 'ƒ', group: 'Advanced',
    render: ({ field, value }) => (
      <input className="df-input" disabled readOnly value={(value as string) ?? ''}
        title={`expression: ${field.config?.expression ?? ''}`} />
    ),
  },
  {
    type: 'HIDDEN', label: 'Hidden', icon: '🚫', group: 'Advanced',
    render: ({ field }) => <em className="df-muted">hidden field: {field.fieldKey}</em>,
  },
  {
    type: 'SECTION_HEADER', label: 'Section Header', icon: '§', group: 'Layout', presentational: true,
    render: ({ field }) => <h3 className="df-section">{field.label}</h3>,
  },
  {
    type: 'REPEATING_GROUP', label: 'Repeating Group', icon: '⧉', group: 'Layout',
    render: ({ field }) => (
      <div className="df-repeating">Repeating group: {field.label} (add child fields in config)</div>
    ),
  },
];

export const fieldRegistry: Record<FieldType, FieldDescriptor> = descriptors.reduce(
  (acc, d) => { acc[d.type] = d; return acc; },
  {} as Record<FieldType, FieldDescriptor>,
);

export const fieldDescriptors = descriptors;

export function descriptorFor(type: FieldType): FieldDescriptor {
  return fieldRegistry[type] ?? fieldRegistry.TEXT;
}
