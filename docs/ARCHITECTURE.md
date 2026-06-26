# Architecture

## Overview

```
┌──────────────────────────┐        REST / JSON         ┌────────────────────────────────┐
│  React + TS (Vite)       │  ───────────────────────►  │  Spring Boot 3.5 (Java)        │
│  - persistent sidebar    │                            │  - Controllers (REST)          │
│  - drag-drop builder     │  ◄───────────────────────  │  - Services (domain logic)     │
│  - field registry        │                            │  - Field validator strategies  │
│  - JSON-schema renderer  │                            │  - Prefill engine (connectors) │
└──────────────────────────┘                            │  - JPA repositories            │
                                                         └───────────────┬────────────────┘
                                                                         │ JPA / JSONB
                                                                ┌────────▼─────────┐  ┌─────────┐
                                                                │   PostgreSQL     │  │  Redis  │
                                                                │ (JSONB columns)  │  │(optional)│
                                                                └──────────────────┘  └─────────┘
```

## Backend package structure (`com.docgen`)

| Package | Responsibility |
|---------|----------------|
| `config` | JPA auditing/repositories, CORS, OpenAPI |
| `common` | `BaseEntity`, `ApiError`, `GlobalExceptionHandler`, `Json` helper, domain exceptions |
| `security` | `JwtService`, `JwtAuthFilter`, `SecurityConfig` (dev/prod), `CryptoService` (AES-GCM), `MaskingService` |
| `user` | `User`, `Organization`, `CurrentTenant`, auth controller (register/login stub) |
| `field` | `FieldType` enum, `FieldValidator` strategy, `Validators`, `FieldValidatorRegistry`, metadata endpoint |
| `template` | `FormTemplate`, `FormTemplateVersion`, `FormField`, `FieldOption`, service, controller |
| `connector` | `ApiConnector`, `ApiFieldMapping`, `ApiConnectorClient` (auth strategies), service, controller |
| `prefill` | `PrefillService` (JSONPath + transform + fallback), `TransformFunctions`, `PrefillRateLimiter` |
| `submission` | `FormSubmission`, `FormSubmissionValue`, service (validation + encryption + masking), controller |
| `audit` | `AuditLog`, `AuditService`, audit controller |
| `extension` | Marker interfaces for planned capabilities (see Extension points) |

## Design patterns (the "flexibility" requirements)

1. **Field registry + strategy pattern.** Backend: `FieldValidator` (one per `FieldType`) collected
   into `FieldValidatorRegistry`. Frontend: `fields/registry.tsx` maps each type to palette metadata,
   default config and a renderer. **Adding a field type** = enum value + a validator bean (backend)
   and one registry descriptor (frontend). Nothing else changes.
2. **Plugin-style connectors.** `ApiConnectorClient` resolves an `AuthStrategy` per `AuthType`
   (NONE/BASIC/BEARER/API_KEY/OAUTH2). Adding an auth scheme = one strategy bean.
3. **JSON-schema-driven rendering.** Templates carry `schemaJson`/`layoutJson`/`validationJson`/
   `roleAccessJson` (JSONB); the React renderer interprets them — no per-template code.
4. **Versioned templates.** Publishing snapshots the full definition into `FormTemplateVersion`
   (immutable), enabling history and rollback.
5. **Metadata-driven config.** Validation rules, conditional visibility, masking and calculated
   expressions all live in `configJson` and are interpreted at runtime.

## Data model

```
Organization 1───* User
Organization 1───* FormTemplate 1───* FormField 1───* FieldOption
                   FormTemplate 1───* FormTemplateVersion
                   FormTemplate 1───* ApiFieldMapping *───1 ApiConnector
                   FormTemplate 1───* FormSubmission 1───* FormSubmissionValue
AuditLog (cross-cutting)
```

Extensible JSONB columns: `FormTemplate.{schema,layout,validation,roleAccess}Json`,
`FormField.configJson`, `ApiConnector.{headers,queryParams}Json`,
`FormTemplateVersion.snapshotJson`, `AuditLog.detailJson`.

## Prefill flow

```
POST /api/forms/{templateId}/prefill { input }
  → load ApiFieldMappings for template, group by connector
  → for each connector: ApiConnectorClient.execute (auth strategy + {{var}} templating, virtual threads)
  → for each mapping: JSONPath extract → transform function → fallback → required check
  → return { fieldKey: value } + per-field details + warnings
Rate limited per template (Redis token bucket, in-memory fallback).
```

## Security model

- **AuthN/AuthZ:** JWT (HS256) via `JwtService`/`JwtAuthFilter`. `dev` profile permits all but still
  parses tokens; `prod` profile requires auth and gates by role (`@EnableMethodSecurity`).
- **Encryption at rest:** `CryptoService` (AES-256-GCM) encrypts connector secrets and sensitive
  submission values.
- **Field masking:** `MaskingService` masks SSN/MPI on read unless the caller has `DATA_UNMASK`.
- **Validation:** Jakarta Validation on DTOs + server-side field validation via the strategy registry.
- **Audit:** `AuditService` records template edits, publishes, clones, mapping changes and submissions.

## Extension points (`com.docgen.extension`)

Reserved seams — implement a Spring bean to activate, no core rewrite:
`DocumentGenerator` (PDF/DOCX), `WorkflowEngine` (approvals), `WebhookPublisher`,
`ESignatureProvider`, `RulesEngine`, `DataStandardMapper` (FHIR/healthcare). Multi-tenant isolation
is seeded by the `organizationId` already carried on templates, connectors and submissions.
