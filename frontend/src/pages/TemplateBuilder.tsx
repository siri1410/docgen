import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import type { FieldInput } from '../api/client';
import { FieldPalette } from '../components/FieldPalette';
import { FieldPropertiesPanel } from '../components/FieldPropertiesPanel';
import { FormBuilderCanvas } from '../components/FormBuilderCanvas';
import { descriptorFor } from '../fields/registry';
import { useTemplate, useTemplateActions, useTemplateVersionsToggle } from '../hooks/queries';
import type { FieldType } from '../types';

export function TemplateBuilder() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: template, isLoading } = useTemplate(id);
  const actions = useTemplateActions(id);
  const versions = useTemplateVersionsToggle(id);
  const [selectedId, setSelectedId] = useState<string | undefined>();

  if (isLoading || !template) return <div className="df-muted">Loading builder…</div>;

  const fields = template.fields;
  const selected = fields.find((f) => f.id === selectedId);

  const addType = (type: FieldType) => {
    const d = descriptorFor(type);
    const key = `${type.toLowerCase()}_${fields.length + 1}`;
    actions.addField.mutate({
      fieldKey: key,
      label: d.label,
      type,
      required: false,
      orderIndex: fields.length,
      config: d.defaultConfig ?? {},
      options: d.optionBacked ? [{ value: 'option1', label: 'Option 1' }] : undefined,
    });
  };

  const reorder = (orderedIds: string[]) => {
    orderedIds.forEach((fid, idx) => {
      const f = fields.find((x) => x.id === fid);
      if (f && f.orderIndex !== idx) {
        const body: FieldInput = {
          fieldKey: f.fieldKey, label: f.label, type: f.type, required: f.required,
          orderIndex: idx, config: f.config, options: f.options,
        };
        actions.updateField.mutate({ fieldId: fid, body });
      }
    });
  };

  return (
    <div>
      <div className="df-toolbar">
        <Link to="/templates" className="df-btn ghost">← Templates</Link>
        <h2 style={{ margin: 0 }}>{template.name}</h2>
        <span className={`df-badge ${template.status.toLowerCase()}`}>{template.status}</span>
        <span className="df-muted">v{template.currentVersion}</span>
        <div className="df-spacer" />
        <Link to={`/templates/${id}/mappings`} className="df-btn ghost">API Mappings</Link>
        <Link to={`/templates/${id}/submissions`} className="df-btn ghost">Submissions</Link>
        <Link to={`/templates/${id}/preview`} className="df-btn secondary">Preview</Link>
        <button className="df-btn secondary" onClick={() => versions.toggle()}>Versions</button>
        <button className="df-btn secondary"
          onClick={async () => {
            const t = await actions.clone.mutateAsync(`${template.name} (copy)`);
            navigate(`/templates/${t.id}/build`);
          }}>Clone</button>
        <button className="df-btn" onClick={() => actions.publish.mutate(undefined)}>Publish</button>
      </div>

      {versions.open && (
        <div className="df-card">
          <h3 style={{ marginTop: 0 }}>Version history</h3>
          {versions.data?.length ? (
            <table className="df-table">
              <thead><tr><th>Version</th><th>By</th><th>Notes</th><th>When</th></tr></thead>
              <tbody>
                {versions.data.map((v) => (
                  <tr key={v.id}>
                    <td>v{v.versionNumber}</td><td>{v.publishedBy ?? '—'}</td>
                    <td>{v.notes ?? '—'}</td><td>{new Date(v.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <div className="df-muted">No published versions yet.</div>}
        </div>
      )}

      <div className="df-builder">
        <div>
          <h3>Field Library</h3>
          <FieldPalette onAdd={addType} />
        </div>
        <div>
          <h3>Canvas</h3>
          <FormBuilderCanvas
            fields={fields}
            selectedId={selectedId}
            onSelect={setSelectedId}
            onAddType={addType}
            onReorder={reorder}
            onDelete={(fid) => { actions.deleteField.mutate(fid); if (fid === selectedId) setSelectedId(undefined); }}
          />
        </div>
        <div>
          <h3>Properties</h3>
          <FieldPropertiesPanel
            field={selected}
            otherFields={fields}
            onSave={(fieldId, body) => actions.updateField.mutate({ fieldId, body })}
          />
        </div>
      </div>
    </div>
  );
}
