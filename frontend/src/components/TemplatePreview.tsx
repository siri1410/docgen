import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client';
import type { DocumentFormat, PrefillResult, Submission, Template } from '../types';
import { DocumentHeader, brandingOf } from './DocumentHeader';
import { FieldRenderer } from './FieldRenderer';

// Renders a live, fillable form from a template's fields (registry-driven),
// with the ability to run the prefill engine, lock fetched values, print, and submit.
export function TemplatePreview({ template }: { template: Template }) {
  const branding = brandingOf(template);
  const lookup = branding.lookup ?? { key: 'memberId', label: 'Member ID', placeholder: 'M-1001' };

  const [values, setValues] = useState<Record<string, unknown>>({});
  const [lookupValue, setLookupValue] = useState(lookup.placeholder ?? '');
  const [prefill, setPrefill] = useState<PrefillResult | null>(null);
  const [submitted, setSubmitted] = useState<Submission | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [docFormats, setDocFormats] = useState<DocumentFormat[]>([]);

  useEffect(() => {
    api.documentFormats().then(setDocFormats).catch(() => setDocFormats([]));
  }, []);

  const setValue = (key: string, v: unknown) => setValues((prev) => ({ ...prev, [key]: v }));

  // Fields whose value came from the fetched record are locked (read-only): the keys
  // returned by the last prefill, plus any field flagged `autofill` that now has a value.
  const lockedKeys = useMemo(() => {
    const s = new Set<string>();
    if (prefill) Object.keys(prefill.values).forEach((k) => s.add(k));
    template.fields.forEach((f) => {
      const v = values[f.fieldKey];
      if (f.config?.autofill && v != null && v !== '') s.add(f.fieldKey);
    });
    return s;
  }, [prefill, values, template.fields]);

  const runPrefill = async () => {
    setError(null);
    try {
      const result = await api.prefill(template.id, { [lookup.key]: lookupValue });
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
      <div className="df-toolbar df-no-print">
        <label className="df-muted" style={{ fontSize: 13 }}>{lookup.label ?? 'Lookup'}</label>
        <input className="df-input" style={{ width: 180 }} value={lookupValue}
          onChange={(e) => setLookupValue(e.target.value)} placeholder={lookup.placeholder} />
        <button className="df-btn secondary" onClick={runPrefill}>⚡ Fetch &amp; prefill</button>
        <div className="df-spacer" />
        <button className="df-btn ghost" onClick={() => window.print()}>🖨 Print / Save as PDF</button>
        <button className="df-btn" onClick={submit}>Submit form</button>
      </div>

      {error && <div className="df-warn df-no-print" style={{ marginBottom: 12 }}>{error}</div>}
      {prefill?.warnings?.length ? (
        <div className="df-warn df-no-print" style={{ marginBottom: 12 }}>
          {prefill.warnings.map((w, i) => <div key={i}>⚠ {w}</div>)}
        </div>
      ) : null}
      {prefill && !prefill.warnings?.length ? (
        <div className="df-toast df-no-print" style={{ marginBottom: 12 }}>
          ✓ Record fetched. {lockedKeys.size} field(s) prefilled and locked; remaining fields are editable.
        </div>
      ) : null}
      {submitted && (
        <div className="df-toast df-no-print" style={{ marginBottom: 12 }}>
          <div>✓ Submitted (id {submitted.id.slice(0, 8)}…). Sensitive values are masked on read-back.</div>
          <div className="df-row" style={{ marginTop: 8, alignItems: 'center' }}>
            <span className="df-muted">Generate document:</span>
            {docFormats.map((f) => (
              <button key={f.format} className="df-btn secondary"
                onClick={() => api.downloadDocument(submitted.id, f.format)}>
                ⬇ {f.label}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* The printable document: branded header + the form. */}
      <div className="df-printable">
        <DocumentHeader template={template} />
        <div className="df-card df-document">
          {template.fields.length === 0 && <div className="df-muted">This template has no fields yet.</div>}
          {template.fields.map((f) => (
            <FieldRenderer key={f.id} field={f} value={values[f.fieldKey]} values={values}
              onChange={(v) => setValue(f.fieldKey, v)} locked={lockedKeys.has(f.fieldKey)} />
          ))}
          <div className="df-print-only df-doc-signature">
            <div><span className="df-sig-line" /> Signature</div>
            <div><span className="df-sig-line short" /> Date</div>
          </div>
        </div>
      </div>
    </div>
  );
}

function extractError(e: unknown): string {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  return err.response?.data?.message ?? err.message ?? 'Request failed';
}
