import { useQuery } from '@tanstack/react-query';
import { useFieldTypes } from '../hooks/queries';
import { http } from '../api/client';

interface AuditEntry {
  id: string; actor: string; action: string; entityType: string; createdAt: string;
}

export function AdminSettings() {
  const fieldTypes = useFieldTypes();
  const audit = useQuery({
    queryKey: ['audit'],
    queryFn: () => http.get<AuditEntry[]>('/audit').then((r) => r.data),
  });

  return (
    <div>
      <h2>Settings</h2>

      <div className="df-card">
        <h3 style={{ marginTop: 0 }}>Supported field types ({fieldTypes.data?.length ?? 0})</h3>
        <p className="df-muted">The backend is the single source of truth; the frontend registry mirrors it.</p>
        <div className="df-row">
          {fieldTypes.data?.map((t) => (
            <span key={t.type} className="df-badge" style={{ margin: 2 }}>
              {t.type}{t.sensitive ? ' 🔒' : ''}
            </span>
          ))}
        </div>
      </div>

      <div className="df-card">
        <h3 style={{ marginTop: 0 }}>Audit log</h3>
        <table className="df-table">
          <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Entity</th></tr></thead>
          <tbody>
            {audit.data?.slice(0, 25).map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.createdAt).toLocaleString()}</td>
                <td>{a.actor}</td><td>{a.action}</td><td>{a.entityType}</td>
              </tr>
            ))}
            {audit.data?.length === 0 && <tr><td colSpan={4} className="df-muted">No audit entries yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
