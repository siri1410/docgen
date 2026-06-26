import { Link } from 'react-router-dom';
import { useConnectors, useTemplates } from '../hooks/queries';

export function Dashboard() {
  const templates = useTemplates();
  const connectors = useConnectors();

  const published = templates.data?.filter((t) => t.status === 'PUBLISHED').length ?? 0;

  return (
    <div>
      <h2>Dashboard</h2>
      <p className="df-muted">A flexible, extensible form/document generation platform.</p>

      <div className="df-grid">
        <div className="df-card">
          <div className="df-muted">Templates</div>
          <div style={{ fontSize: 32, fontWeight: 700 }}>{templates.data?.length ?? '—'}</div>
          <Link to="/templates">Manage templates →</Link>
        </div>
        <div className="df-card">
          <div className="df-muted">Published</div>
          <div style={{ fontSize: 32, fontWeight: 700 }}>{published}</div>
        </div>
        <div className="df-card">
          <div className="df-muted">API Connectors</div>
          <div style={{ fontSize: 32, fontWeight: 700 }}>{connectors.data?.length ?? '—'}</div>
          <Link to="/connectors">Configure connectors →</Link>
        </div>
      </div>

      <div className="df-card">
        <h3 style={{ marginTop: 0 }}>Quick start</h3>
        <ol className="df-muted">
          <li>Open a template and drag fields onto the canvas.</li>
          <li>Configure an API connector, then map JSONPath responses to fields.</li>
          <li>Preview the form, run prefill, and submit — sensitive values are masked + encrypted.</li>
        </ol>
      </div>
    </div>
  );
}
