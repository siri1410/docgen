import { useState } from 'react';
import { api } from '../api/client';
import { useConnectors } from '../hooks/queries';
import { useQueryClient } from '@tanstack/react-query';
import type { AuthType } from '../types';

export function ConnectorSetup() {
  const { data: connectors } = useConnectors();
  const qc = useQueryClient();
  const [form, setForm] = useState({
    name: '', baseUrl: '', httpMethod: 'GET', authType: 'NONE' as AuthType,
    headers: '{"Accept":"application/json"}', secret: '',
  });
  const [testResult, setTestResult] = useState<string | null>(null);

  const create = async () => {
    await api.createConnector({
      name: form.name,
      baseUrl: form.baseUrl,
      httpMethod: form.httpMethod,
      authType: form.authType,
      headers: safeJson(form.headers),
      secret: form.secret ? safeJson(form.secret) : undefined,
    });
    setForm({ ...form, name: '', baseUrl: '', secret: '' });
    qc.invalidateQueries({ queryKey: ['connectors'] });
  };

  const test = async (id: string) => {
    setTestResult('Testing…');
    try {
      const r = await api.testConnector(id, { memberId: 'M-1' });
      setTestResult(JSON.stringify(r, null, 2));
    } catch (e) {
      setTestResult(`Error: ${(e as Error).message}`);
    }
  };

  return (
    <div>
      <h2>API Connectors</h2>
      <p className="df-muted">
        Configure external REST APIs used to prefill forms. Secrets are encrypted at rest (AES-GCM).
      </p>

      <div className="df-card">
        <h3 style={{ marginTop: 0 }}>New connector</h3>
        <div className="df-row">
          <div style={{ flex: 1 }}>
            <label className="df-label">Name</label>
            <input className="df-input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </div>
          <div style={{ flex: 2 }}>
            <label className="df-label">Base URL (supports {'{{var}}'} placeholders)</label>
            <input className="df-input" placeholder="https://api.example.com/members/{{memberId}}"
              value={form.baseUrl} onChange={(e) => setForm({ ...form, baseUrl: e.target.value })} />
          </div>
        </div>
        <div className="df-row" style={{ marginTop: 8 }}>
          <div>
            <label className="df-label">Method</label>
            <select className="df-input" value={form.httpMethod}
              onChange={(e) => setForm({ ...form, httpMethod: e.target.value })}>
              {['GET', 'POST', 'PUT'].map((m) => <option key={m}>{m}</option>)}
            </select>
          </div>
          <div>
            <label className="df-label">Auth type</label>
            <select className="df-input" value={form.authType}
              onChange={(e) => setForm({ ...form, authType: e.target.value as AuthType })}>
              {['NONE', 'BASIC', 'BEARER', 'API_KEY', 'OAUTH2'].map((a) => <option key={a}>{a}</option>)}
            </select>
          </div>
          <div style={{ flex: 1 }}>
            <label className="df-label">Headers (JSON)</label>
            <input className="df-input" value={form.headers}
              onChange={(e) => setForm({ ...form, headers: e.target.value })} />
          </div>
          <div style={{ flex: 1 }}>
            <label className="df-label">Secret (JSON, encrypted)</label>
            <input className="df-input" placeholder='{"token":"..."}' value={form.secret}
              onChange={(e) => setForm({ ...form, secret: e.target.value })} />
          </div>
        </div>
        <button className="df-btn" style={{ marginTop: 12 }} onClick={create}>+ Create connector</button>
      </div>

      <div className="df-card">
        <table className="df-table">
          <thead><tr><th>Name</th><th>URL</th><th>Auth</th><th>Secret</th><th /></tr></thead>
          <tbody>
            {connectors?.map((c) => (
              <tr key={c.id}>
                <td><strong>{c.name}</strong></td>
                <td className="df-muted">{c.httpMethod} {c.baseUrl}</td>
                <td>{c.authType}</td>
                <td>{c.hasSecret ? '🔒 set' : '—'}</td>
                <td><button className="df-btn secondary" onClick={() => test(c.id)}>Test</button></td>
              </tr>
            ))}
            {connectors?.length === 0 && <tr><td colSpan={5} className="df-muted">No connectors yet.</td></tr>}
          </tbody>
        </table>
      </div>

      {testResult && (
        <div className="df-card">
          <h3 style={{ marginTop: 0 }}>Test result</h3>
          <pre style={{ whiteSpace: 'pre-wrap', fontSize: 12 }}>{testResult}</pre>
        </div>
      )}
    </div>
  );
}

function safeJson(s: string): Record<string, unknown> {
  try { return JSON.parse(s); } catch { return {}; }
}
