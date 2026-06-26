import { Link, useParams } from 'react-router-dom';
import { TemplatePreview } from '../components/TemplatePreview';
import { useTemplate } from '../hooks/queries';

export function FormPreviewPage() {
  const { id } = useParams();
  const { data: template, isLoading } = useTemplate(id);

  if (isLoading || !template) return <div className="df-muted">Loading…</div>;

  return (
    <div>
      <div className="df-toolbar">
        <Link to={`/templates/${id}/build`} className="df-btn ghost">← Builder</Link>
        <h2 style={{ margin: 0 }}>Preview · {template.name}</h2>
      </div>
      <TemplatePreview template={template} />
    </div>
  );
}
