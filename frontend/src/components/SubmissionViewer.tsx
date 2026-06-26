import { useState } from 'react';
import { api } from '../api/client';
import type { Submission, SubmissionSummary } from '../types';

// Lists submissions for a template and shows a selected submission's values
// (sensitive fields arrive already masked from the backend unless authorized).
export function SubmissionViewer({ submissions }: { submissions: SubmissionSummary[] }) {
  const [selected, setSelected] = useState<Submission | null>(null);

  const open = async (id: string) => setSelected(await api.getSubmission(id));

  return (
    <div className="df-row" style={{ alignItems: 'flex-start' }}>
      <div className="df-card" style={{ flex: 1 }}>
        <h3 style={{ marginTop: 0 }}>Submissions ({submissions.length})</h3>
        <table className="df-table">
          <thead><tr><th>ID</th><th>By</th><th>Fields</th><th>When</th><th /></tr></thead>
          <tbody>
            {submissions.map((s) => (
              <tr key={s.id}>
                <td>{s.id.slice(0, 8)}…</td>
                <td>{s.submittedBy ?? '—'}</td>
                <td>{s.fieldCount}</td>
                <td>{new Date(s.createdAt).toLocaleString()}</td>
                <td><button className="df-btn secondary" onClick={() => open(s.id)}>View</button></td>
              </tr>
            ))}
            {submissions.length === 0 && (
              <tr><td colSpan={5} className="df-muted">No submissions yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="df-card" style={{ flex: 1 }}>
          <h3 style={{ marginTop: 0 }}>Submission {selected.id.slice(0, 8)}…</h3>
          <table className="df-table">
            <thead><tr><th>Field</th><th>Value</th></tr></thead>
            <tbody>
              {Object.entries(selected.values).map(([k, v]) => (
                <tr key={k}><td>{k}</td><td>{JSON.stringify(v)}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
