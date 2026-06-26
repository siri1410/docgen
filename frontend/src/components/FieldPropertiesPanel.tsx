import { useEffect, useState } from 'react';
import type { FieldInput } from '../api/client';
import { descriptorFor } from '../fields/registry';
import type { FieldConfig, FieldOption, FormField } from '../types';
import { ConditionalLogicBuilder } from './ConditionalLogicBuilder';

interface Props {
  field: FormField | undefined;
  otherFields: FormField[];
  onSave: (fieldId: string, body: FieldInput) => void;
}

// Edits the selected field. Reads/writes config + options; reflects the field
// registry's metadata (e.g. only option-backed types show the options editor).
export function FieldPropertiesPanel({ field, otherFields, onSave }: Props) {
  const [draft, setDraft] = useState<FormField | undefined>(field);

  useEffect(() => setDraft(field), [field]);

  if (!draft || !field) {
    return <div className="df-card df-muted">Select a field to edit its properties.</div>;
  }

  const descriptor = descriptorFor(draft.type);
  const config: FieldConfig = draft.config ?? {};
  const update = (patch: Partial<FormField>) => setDraft({ ...draft, ...patch });
  const updateConfig = (patch: Partial<FieldConfig>) => update({ config: { ...config, ...patch } });

  const setOptions = (options: FieldOption[]) => update({ options });

  const save = () => onSave(field.id, {
    fieldKey: draft.fieldKey,
    label: draft.label,
    type: draft.type,
    required: draft.required,
    orderIndex: draft.orderIndex,
    config: draft.config,
    options: draft.options,
  });

  return (
    <div className="df-card">
      <h3 style={{ marginTop: 0 }}>{descriptor.icon} Field properties</h3>

      <div className="df-field">
        <label className="df-label">Label</label>
        <input className="df-input" value={draft.label} onChange={(e) => update({ label: e.target.value })} />
      </div>

      <div className="df-field">
        <label className="df-label">Field key</label>
        <input className="df-input" value={draft.fieldKey}
          onChange={(e) => update({ fieldKey: e.target.value })} />
      </div>

      {!descriptor.presentational && (
        <label className="df-radio" style={{ marginBottom: 12 }}>
          <input type="checkbox" checked={draft.required}
            onChange={(e) => update({ required: e.target.checked })} />
          Required
        </label>
      )}

      {['TEXT', 'EMAIL', 'PHONE', 'NUMBER', 'SSN', 'MPI'].includes(draft.type) && (
        <div className="df-field">
          <label className="df-label">Placeholder</label>
          <input className="df-input" value={(config.placeholder as string) ?? ''}
            onChange={(e) => updateConfig({ placeholder: e.target.value })} />
        </div>
      )}

      {draft.type === 'NUMBER' && (
        <div className="df-row">
          <div style={{ flex: 1 }}>
            <label className="df-label">Min</label>
            <input className="df-input" type="number" value={(config.min as number) ?? ''}
              onChange={(e) => updateConfig({ min: e.target.value === '' ? undefined : Number(e.target.value) })} />
          </div>
          <div style={{ flex: 1 }}>
            <label className="df-label">Max</label>
            <input className="df-input" type="number" value={(config.max as number) ?? ''}
              onChange={(e) => updateConfig({ max: e.target.value === '' ? undefined : Number(e.target.value) })} />
          </div>
        </div>
      )}

      {draft.type === 'CALCULATED' && (
        <div className="df-field">
          <label className="df-label">Expression</label>
          <input className="df-input" value={(config.expression as string) ?? ''}
            placeholder="e.g. {qty} * {price}"
            onChange={(e) => updateConfig({ expression: e.target.value })} />
        </div>
      )}

      {descriptor.optionBacked && (
        <div className="df-field">
          <label className="df-label">Options</label>
          {(draft.options ?? []).map((o, i) => (
            <div className="df-row" key={i} style={{ marginBottom: 4 }}>
              <input className="df-input" placeholder="value" value={o.value}
                onChange={(e) => setOptions((draft.options ?? []).map((x, j) =>
                  j === i ? { ...x, value: e.target.value } : x))} />
              <input className="df-input" placeholder="label" value={o.label}
                onChange={(e) => setOptions((draft.options ?? []).map((x, j) =>
                  j === i ? { ...x, label: e.target.value } : x))} />
              <button className="df-btn ghost"
                onClick={() => setOptions((draft.options ?? []).filter((_, j) => j !== i))}>✕</button>
            </div>
          ))}
          <button className="df-btn secondary"
            onClick={() => setOptions([...(draft.options ?? []), { value: '', label: '' }])}>
            + Add option
          </button>
        </div>
      )}

      <div className="df-field">
        <label className="df-label">Help text</label>
        <input className="df-input" value={(config.helpText as string) ?? ''}
          onChange={(e) => updateConfig({ helpText: e.target.value })} />
      </div>

      <div className="df-field">
        <ConditionalLogicBuilder
          value={config.visibleWhen}
          otherFields={otherFields.filter((f) => f.id !== field.id)}
          onChange={(cond) => updateConfig({ visibleWhen: cond })}
        />
      </div>

      <button className="df-btn" style={{ width: '100%', marginTop: 8 }} onClick={save}>
        Save field
      </button>
    </div>
  );
}
