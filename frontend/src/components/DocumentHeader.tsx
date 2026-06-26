import type { Template } from '../types';

// Branding pulled from the template's schema_json: { department, logo, title, agency }.
export interface Branding {
  department?: string;
  logo?: string;
  title?: string;
  agency?: string;
  lookup?: { key: string; label?: string; placeholder?: string };
}

export function brandingOf(template: Template): Branding {
  return (template.schema as Branding) ?? {};
}

// Official-looking document header shown above the form (and on the printed page).
// Department + logo come from branding; falls back to the template name.
export function DocumentHeader({ template }: { template: Template }) {
  const b = brandingOf(template);
  const department = b.department ?? b.agency ?? 'Forms Service';
  const title = b.title ?? template.name;
  return (
    <header className="df-doc-header">
      <div className="df-doc-seal" aria-hidden>{b.logo ?? '🏛'}</div>
      <div className="df-doc-headings">
        <div className="df-doc-dept">{department}</div>
        <h1 className="df-doc-title">{title}</h1>
        {template.description && <div className="df-doc-sub">{template.description}</div>}
      </div>
      <div className="df-doc-meta">
        <div>Form: {template.slug}</div>
        <div>Version {template.currentVersion}</div>
      </div>
    </header>
  );
}
