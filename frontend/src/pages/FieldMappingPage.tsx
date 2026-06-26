import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { JsonPathMapper } from '../components/JsonPathMapper';
import { useConnectors, useMappings, useTemplate, useTemplateActions } from '../hooks/queries';
import type { FieldMapping } from '../types';

export function FieldMappingPage() {
  const { id } = useParams();
  const { data: template } = useTemplate(id);
  const { data: connectors } = useConnectors();
  const { data: existing } = useMappings(id);
  const actions = useTemplateActions(id);
  const [rows, setRows] = useState<FieldMapping[]>([]);
  const [saved, setSaved] = useState(false);

  useEffect(() => { if (existing) setRows(existing); }, [existing]);

  if (!template || !connectors) return <div className="df-muted">Loading…</div>;

  const addRow = () => setRows([...rows, {
    connectorId: connectors[0]?.id ?? '', source: '', target: '', required: false,
  }]);

  const save = async () => {
    await actions.saveMappings.mutateAsync(rows.filter((r) => r.connectorId && r.source && r.target));
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  return (
    <div>
      <div className="df-toolbar">
        <Link to={`/templates/${id}/build`} className="df-btn ghost">← Builder</Link>
        <h2 style={{ margin: 0 }}>API Field Mappings · {template.name}</h2>
        <div className="df-spacer" />
        <button className="df-btn secondary" onClick={addRow} disabled={connectors.length === 0}>+ Mapping</button>
        <button className="df-btn" onClick={save}>Save mappings</button>
      </div>

      {connectors.length === 0 && (
        <div className="df-warn">Create an API connector first on the Connectors page.</div>
      )}
      {saved && <div className="df-toast">✓ Mappings saved.</div>}

      <div className="df-card">
        <table className="df-table">
          <thead>
            <tr><th>Connector</th><th>Source (JSONPath)</th><th>Target field</th>
              <th>Transform</th><th>Fallback</th><th>Req</th><th /></tr>
          </thead>
          <tbody>
            {rows.map((m, i) => (
              <JsonPathMapper key={i} mapping={m} connectors={connectors} fields={template.fields}
                onChange={(next) => setRows(rows.map((r, j) => (j === i ? next : r)))}
                onRemove={() => setRows(rows.filter((_, j) => j !== i))} />
            ))}
            {rows.length === 0 && <tr><td colSpan={7} className="df-muted">No mappings configured.</td></tr>}
          </tbody>
        </table>
      </div>

      <p className="df-muted">
        Example: <code>$.member.firstName → firstName</code> with transform <code>capitalize</code>.
        Mappings drive <code>POST /api/forms/{'{templateId}'}/prefill</code>.
      </p>
    </div>
  );
}
