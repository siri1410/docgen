import { useState } from 'react';
import { api } from '../api/client';
import type { PrefillResult, Submission, Template } from '../types';
import { FieldRenderer } from './FieldRenderer';

// Renders a live, fillable form from a template's fields (registry-driven),
// with the ability to run the prefill engine and submit to the backend.
export function TemplatePreview({ template }: { template: Template }) {
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [memberId, setMemberId] = useState('M-1001');
  const [prefill, setPrefill] = useState<PrefillResult | null>(null);
  const [submitted, setSubmitted] = useState<Submission | null>(null);
  const [error, setError] = useState<string | null>(null);

  const setValue = (key: string, v: unknown) => setValues((prev) => ({ ...prev, [key]: v }));

  const runPrefill = async () => {
    setError(null);
    try {
      const result = await api.prefill(template.id, { memberId });
      setPrefill(result);
      setValues((prev) => ({ ...prev, ...result.values }));
    } catch (e) {
      setError(extractError(e));
    }
  };

  const submit = async () => {
    setError(null);
    setSubmitted(null);
    try {
      const result = await api.submit(template.id, values);
      setSubmitted(result);
    } catch (e) {
      setError(extractError(e));
    }
  };

  return (
    <div>
      <div className="df-toolbar">
        <input className="df-input" style={{ width: 160 }} value={memberId}
          onChange={(e) => setMemberId(e.target.value)} placeholder="memberId for prefill" />
        <button className="df-btn secondary" onClick={runPrefill}>⚡ Run prefill</button>
        <div className="df-spacer" />
        <button className="df-btn" onClick={submit}>Submit form</button>
      </div>

      {error && <div className="df-warn" style={{ marginBottom: 12 }}>{error}</div>}
      {prefill?.warnings?.length ? (
        <div className="df-warn" style={{ marginBottom: 12 }}>
          {prefill.warnings.map((w, i) => <div key={i}>⚠ {w}</div>)}
        </div>
      ) : null}
      {submitted && (
        <div className="df-toast" style={{ marginBottom: 12 }}>
          ✓ Submitted (id {submitted.id.slice(0, 8)}…). Sensitive values are masked on read-back.
        </div>
      )}

      <div className="df-card">
        {template.fields.length === 0 && <div className="df-muted">This template has no fields yet.</div>}
        {template.fields.map((f) => (
          <FieldRenderer key={f.id} field={f} value={values[f.fieldKey]} values={values}
            onChange={(v) => setValue(f.fieldKey, v)} />
        ))}
      </div>
    </div>
  );
}

function extractError(e: unknown): string {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  return err.response?.data?.message ?? err.message ?? 'Request failed';
}
