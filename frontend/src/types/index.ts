// Shared types mirroring the backend DTOs.

export type FieldType =
  | 'TEXT' | 'NUMBER' | 'DATE' | 'DROPDOWN' | 'CHECKBOX' | 'RADIO'
  | 'FILE_UPLOAD' | 'ADDRESS' | 'SSN' | 'MPI' | 'EMAIL' | 'PHONE'
  | 'SIGNATURE' | 'CALCULATED' | 'HIDDEN' | 'SECTION_HEADER' | 'REPEATING_GROUP';

export type TemplateStatus = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED';

export type AuthType = 'NONE' | 'BASIC' | 'BEARER' | 'API_KEY' | 'OAUTH2';

export interface FieldOption {
  value: string;
  label: string;
}

export interface FieldConfig {
  placeholder?: string;
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
  pattern?: string;
  mask?: boolean;
  helpText?: string;
  // Conditional visibility: show this field only when `visibleWhen` matches current form values.
  visibleWhen?: { field: string; operator: 'eq' | 'neq' | 'in' | 'notEmpty'; value?: unknown };
  // For CALCULATED fields.
  expression?: string;
  [key: string]: unknown;
}

export interface FormField {
  id: string;
  fieldKey: string;
  label: string;
  type: FieldType;
  required: boolean;
  sensitive?: boolean;
  orderIndex: number;
  config?: FieldConfig | null;
  options?: FieldOption[];
}

export interface TemplateSummary {
  id: string;
  name: string;
  slug: string;
  category?: string;
  status: TemplateStatus;
  currentVersion: number;
  updatedAt: string;
}

export interface Template extends TemplateSummary {
  description?: string;
  schema?: unknown;
  layout?: unknown;
  validation?: unknown;
  roleAccess?: unknown;
  fields: FormField[];
  createdAt: string;
}

export interface TemplateVersion {
  id: string;
  versionNumber: number;
  publishedBy?: string;
  notes?: string;
  createdAt: string;
}

export interface Connector {
  id: string;
  name: string;
  baseUrl: string;
  httpMethod: string;
  authType: AuthType;
  headers?: Record<string, string> | null;
  queryParams?: Record<string, string> | null;
  requestBodyTemplate?: string | null;
  hasSecret: boolean;
}

export interface FieldMapping {
  id?: string;
  connectorId: string;
  source: string;
  target: string;
  transform?: string;
  fallbackValue?: string;
  required: boolean;
}

export interface PrefillResult {
  values: Record<string, unknown>;
  details: { target: string; value: unknown; source: string; connector: string; fromFallback: boolean }[];
  warnings: string[];
}

export interface Submission {
  id: string;
  templateId: string;
  templateVersion: number;
  submittedBy?: string;
  values: Record<string, unknown>;
  createdAt: string;
}

export interface SubmissionSummary {
  id: string;
  templateId: string;
  submittedBy?: string;
  fieldCount: number;
  createdAt: string;
}

export interface FieldTypeInfo {
  type: FieldType;
  sensitive: boolean;
  optionBacked: boolean;
  presentational: boolean;
}
