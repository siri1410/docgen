import { Link, useParams } from 'react-router-dom';
import { SubmissionViewer } from '../components/SubmissionViewer';
import { useSubmissions, useTemplate } from '../hooks/queries';

export function SubmissionsPage() {
  const { id } = useParams();
  const { data: template } = useTemplate(id);
  const { data: submissions } = useSubmissions(id);

  return (
    <div>
      <div className="df-toolbar">
        <Link to={`/templates/${id}/build`} className="df-btn ghost">← Builder</Link>
        <h2 style={{ margin: 0 }}>Submissions · {template?.name ?? ''}</h2>
      </div>
      <SubmissionViewer submissions={submissions ?? []} />
    </div>
  );
}
