import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCreateTemplate, useTemplates } from '../hooks/queries';

export function TemplateList() {
  const { data, isLoading } = useTemplates();
  const create = useCreateTemplate();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [category, setCategory] = useState('INTAKE');

  const onCreate = async () => {
    if (!name.trim()) return;
    const t = await create.mutateAsync({ name, category });
    setName('');
    navigate(`/templates/${t.id}/build`);
  };

  return (
    <div>
      <h2>Templates</h2>

      <div className="df-card">
        <div className="df-toolbar" style={{ marginBottom: 0 }}>
          <input className="df-input" style={{ width: 240 }} placeholder="New template name"
            value={name} onChange={(e) => setName(e.target.value)} />
          <select className="df-input" style={{ width: 200 }} value={category}
            onChange={(e) => setCategory(e.target.value)}>
            {['INTAKE', 'MEMBER_REGISTRATION', 'ELIGIBILITY', 'CLAIMS', 'CASE_MGMT',
              'ADDRESS_VERIFICATION', 'CUSTOM'].map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
          <button className="df-btn" onClick={onCreate} disabled={create.isPending}>+ Create</button>
        </div>
      </div>

      <div className="df-card">
        {isLoading ? <div className="df-muted">Loading…</div> : (
          <table className="df-table">
            <thead>
              <tr><th>Name</th><th>Category</th><th>Status</th><th>Version</th><th>Updated</th><th /></tr>
            </thead>
            <tbody>
              {data?.map((t) => (
                <tr key={t.id}>
                  <td><strong>{t.name}</strong><div className="df-muted">{t.slug}</div></td>
                  <td>{t.category ?? '—'}</td>
                  <td><span className={`df-badge ${t.status.toLowerCase()}`}>{t.status}</span></td>
                  <td>v{t.currentVersion}</td>
                  <td>{new Date(t.updatedAt).toLocaleDateString()}</td>
                  <td>
                    <Link className="df-btn secondary" to={`/templates/${t.id}/build`}>Build</Link>{' '}
                    <Link className="df-btn ghost" to={`/templates/${t.id}/preview`}>Preview</Link>
                  </td>
                </tr>
              ))}
              {data?.length === 0 && <tr><td colSpan={6} className="df-muted">No templates yet.</td></tr>}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
