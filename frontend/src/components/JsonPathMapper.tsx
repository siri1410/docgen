import type { Connector, FieldMapping, FormField } from '../types';

const TRANSFORMS = ['', 'capitalize', 'upper', 'lower', 'trim', 'titleCase', 'digitsOnly', 'ssnMask'];

interface Props {
  mapping: FieldMapping;
  connectors: Connector[];
  fields: FormField[];
  onChange: (m: FieldMapping) => void;
  onRemove: () => void;
}

// One mapping row: JSONPath source -> target field, with connector, transform,
// fallback and required. This is the visual JSONPath-to-field mapper.
export function JsonPathMapper({ mapping, connectors, fields, onChange, onRemove }: Props) {
  return (
    <tr>
      <td>
        <select className="df-input" value={mapping.connectorId}
          onChange={(e) => onChange({ ...mapping, connectorId: e.target.value })}>
          {connectors.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </td>
      <td>
        <input className="df-input" placeholder="$.member.firstName" value={mapping.source}
          onChange={(e) => onChange({ ...mapping, source: e.target.value })} />
      </td>
      <td>
        <select className="df-input" value={mapping.target}
          onChange={(e) => onChange({ ...mapping, target: e.target.value })}>
          <option value="">— field —</option>
          {fields.map((f) => <option key={f.id} value={f.fieldKey}>{f.fieldKey}</option>)}
        </select>
      </td>
      <td>
        <select className="df-input" value={mapping.transform ?? ''}
          onChange={(e) => onChange({ ...mapping, transform: e.target.value || undefined })}>
          {TRANSFORMS.map((t) => <option key={t} value={t}>{t || '(none)'}</option>)}
        </select>
      </td>
      <td>
        <input className="df-input" placeholder="fallback" value={mapping.fallbackValue ?? ''}
          onChange={(e) => onChange({ ...mapping, fallbackValue: e.target.value || undefined })} />
      </td>
      <td style={{ textAlign: 'center' }}>
        <input type="checkbox" checked={mapping.required}
          onChange={(e) => onChange({ ...mapping, required: e.target.checked })} />
      </td>
      <td><button className="df-btn ghost" onClick={onRemove}>✕</button></td>
    </tr>
  );
}
