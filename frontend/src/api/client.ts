import axios from 'axios';
import type {
  Connector, DocumentFormat, FieldMapping, FieldType, FieldTypeInfo, PrefillResult,
  Submission, SubmissionSummary, Template, TemplateSummary, TemplateVersion,
} from '../types';

// Axios instance. The dev server proxies /api to the Spring Boot backend.
// A JWT (if present) is attached automatically so the same client works in prod mode.
export const http = axios.create({ baseURL: '/api' });

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('docgen.token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface TemplateInput {
  name: string;
  slug?: string;
  description?: string;
  category?: string;
  schema?: unknown;
  layout?: unknown;
  validation?: unknown;
  roleAccess?: unknown;
}

export interface ConnectorInput {
  name: string;
  baseUrl: string;
  httpMethod?: string;
  authType?: string;
  headers?: unknown;
  queryParams?: unknown;
  requestBodyTemplate?: string;
  secret?: unknown;
}

export interface FieldInput {
  fieldKey?: string;
  label: string;
  type: FieldType;
  required: boolean;
  orderIndex?: number;
  config?: unknown;
  options?: { value: string; label: string }[];
}

export const api = {
  // Field type catalogue (backend is the single source of truth).
  fieldTypes: () => http.get<FieldTypeInfo[]>('/field-types').then((r) => r.data),

  // Templates
  listTemplates: () => http.get<TemplateSummary[]>('/templates').then((r) => r.data),
  getTemplate: (id: string) => http.get<Template>(`/templates/${id}`).then((r) => r.data),
  createTemplate: (body: TemplateInput) => http.post<Template>('/templates', body).then((r) => r.data),
  updateTemplate: (id: string, body: TemplateInput) =>
    http.put<Template>(`/templates/${id}`, body).then((r) => r.data),
  publishTemplate: (id: string, notes?: string) =>
    http.post<TemplateVersion>(`/templates/${id}/publish`, { notes }).then((r) => r.data),
  cloneTemplate: (id: string, name?: string) =>
    http.post<Template>(`/templates/${id}/clone`, { name }).then((r) => r.data),
  versions: (id: string) => http.get<TemplateVersion[]>(`/templates/${id}/versions`).then((r) => r.data),

  // Fields
  addField: (templateId: string, body: FieldInput) =>
    http.post(`/templates/${templateId}/fields`, body).then((r) => r.data),
  updateField: (templateId: string, fieldId: string, body: FieldInput) =>
    http.put(`/templates/${templateId}/fields/${fieldId}`, body).then((r) => r.data),
  deleteField: (templateId: string, fieldId: string) =>
    http.delete(`/templates/${templateId}/fields/${fieldId}`).then((r) => r.data),

  // Connectors + mappings
  listConnectors: () => http.get<Connector[]>('/connectors').then((r) => r.data),
  createConnector: (body: ConnectorInput) =>
    http.post<Connector>('/connectors', body).then((r) => r.data),
  testConnector: (id: string, input: Record<string, unknown>) =>
    http.post(`/connectors/${id}/test`, { input }).then((r) => r.data),
  listMappings: (templateId: string) =>
    http.get<FieldMapping[]>(`/templates/${templateId}/mappings`).then((r) => r.data),
  saveMappings: (templateId: string, mappings: FieldMapping[]) =>
    http.post<FieldMapping[]>(`/templates/${templateId}/mappings`, { mappings }).then((r) => r.data),

  // Prefill + submissions
  prefill: (templateId: string, input: Record<string, unknown>) =>
    http.post<PrefillResult>(`/forms/${templateId}/prefill`, { input }).then((r) => r.data),
  submit: (templateId: string, values: Record<string, unknown>) =>
    http.post<Submission>(`/forms/${templateId}/submit`, { values }).then((r) => r.data),
  listSubmissions: (templateId: string) =>
    http.get<SubmissionSummary[]>(`/forms/${templateId}/submissions`).then((r) => r.data),
  getSubmission: (id: string) => http.get<Submission>(`/submissions/${id}`).then((r) => r.data),

  // Server-side document generation (PDF / Word / RTF) via the DocumentGenerator extension point.
  documentFormats: () => http.get<DocumentFormat[]>('/documents/formats').then((r) => r.data),
  downloadDocument: async (submissionId: string, format: string) => {
    const res = await http.get(`/submissions/${submissionId}/document`, {
      params: { format }, responseType: 'blob',
    });
    const cd = String(res.headers['content-disposition'] ?? '');
    const filename = /filename="?([^"]+)"?/.exec(cd)?.[1] ?? `form-${submissionId.slice(0, 8)}.${format}`;
    const url = URL.createObjectURL(res.data as Blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  },
};
