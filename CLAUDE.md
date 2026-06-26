# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

**docgen** — a Doc4j-style form/document-template builder. Spring Boot (Java 21) backend + React/TypeScript (Vite) frontend on PostgreSQL (JSONB) with optional Redis. Users visually build versioned form templates, wire fields to REST APIs for auto-prefill, and submit with server-side validation, encryption, and field masking.

## Toolchain note (important)

The backend requires **Java 21+** (`pom.xml` sets `<java.version>21</java.version>`). On this machine the default `java` on PATH is 17, but Temurin 21 is installed. Build/run the backend with JDK 21 explicitly:

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

The repo root `./start.sh` does this automatically (see Commands).

## Commands

```bash
# Bring everything up (postgres+redis, backend, frontend) — pins JDK 21:
./start.sh

# --- Backend (from backend/) ---
./mvnw spring-boot:run          # run API on :8080 (Flyway migrates + seeds demo data)
./mvnw package                  # build deployable jar
./mvnw test                     # run all tests
./mvnw test -Dtest=PrefillServiceTest             # single test class
./mvnw test -Dtest=PrefillServiceTest#methodName  # single test method

# --- Frontend (from frontend/) ---
npm install
npm run dev                     # Vite dev server on :5173, proxies /api -> :8080
npm run build                   # tsc --noEmit + vite build (static assets)
npm run lint                    # tsc --noEmit (type-check only; no ESLint configured)

# --- Infra ---
docker compose up -d            # postgres :5432, redis :6379
```

Key URLs: app `http://localhost:5173`, Swagger `http://localhost:8080/swagger-ui.html`, health `http://localhost:8080/actuator/health`.

Demo admin (JWT testing): `admin@docgen.local` / `password` via `POST /api/auth/login`. Holds `DATA_UNMASK` role (sees unmasked SSN/MPI).

## Profiles & config

`DOCGEN_PROFILE` (default `dev`) selects the security posture: `dev` is permissive (no login wall, but still parses JWTs); `prod` enforces auth + `@EnableMethodSecurity` role gating. Database, JWT secret, AES key, and Redis are all env-overridable — see the table in `README.md` and defaults in `backend/src/main/resources/application.yml`. Redis is optional: if unreachable, prefill rate limiting silently falls back to an in-memory limiter (`REDIS_HEALTH` defaults to `false` so a missing Redis won't fail the health check).

## Product direction: stay configuration-driven for government/state forms

docgen is meant to serve government agencies and state departments — Driving License, Child Support, eligibility, case management, and similar form families. The guiding constraint: **onboarding a new agency form must be data/configuration, not new code.** Before adding a feature, check whether it can be expressed through the existing seams below.

- **Form `category` is a free-form `String`** on `FormTemplate` (not an enum) — new domains like `DRIVING_LICENSE` or `CHILD_SUPPORT` are just values, no migration needed.
- New form layouts/validation/visibility/calculations → JSONB config on the template/field, interpreted at runtime (no per-form code).
- A new field type an agency needs → enum value + validator bean (backend) + registry descriptor (frontend); see the field-registry seam below.
- A new agency data source for prefill → an `ApiConnector` row (+ an `AuthStrategy` bean only if the auth scheme is new).
- `organizationId` on templates/connectors/submissions is the multi-agency isolation seam.
- Heavier agency needs (PDF/DOCX generation, approval workflows, e-sign, FHIR/data-standard mapping) have reserved interfaces in `com.docgen.extension` — implement a bean, don't fork the core.

Avoid hard-coding any single agency's form, category, or rules into Java/React; push it into template JSON, connectors, and validators.

**Worked example:** `db/migration/V3__seed_driving_license.sql` seeds a "Driving License Renewal" template (category `DRIVING_LICENSE`) that demonstrates the static-vs-dynamic split: static fields (renewal reason, organ-donor, certification) defined on the template, and `autofill` fields (name, DOB, license class, address, expiry) populated by a DMV-lookup connector keyed on the applicant's `dlNumber` input. The connector templates `{{dlNumber}}` into a call and maps `$.args.dl` back to prove input→field flow.

**Demo data sources are local.** `com.docgen.mock.MockDirectoryController` serves `/api/mock/members/{memberId}` and `/api/mock/dmv/{dlNumber}` in-process; the seeded connectors point at `http://localhost:8080/api/mock/...` (see `V4`) so prefill works fully offline with no external network. Response shapes mirror the JSONPath mapping sources (`$.member.*`, `$.record.*`, `$.args.dl`). In a real deployment, repoint the connector `baseUrl` at the agency API and delete the mock — mappings stay identical. (Caveat: the mock URL hard-codes port 8080; if `SERVER_PORT` changes, update the connector rows.)

**Form-rendering conventions (frontend `TemplatePreview`):**

- **Branding** lives in `schema_json`: `{ department, logo, lookup:{key,label,placeholder} }`. `DocumentHeader` renders the department seal + heading; `lookup.key` drives which input the "Fetch & prefill" button sends (e.g. `dlNumber` vs `memberId`).
- **Locked prefill:** any field returned by the last prefill, or flagged `config.autofill:true`, renders read-only with a "🔒 from record" badge — fetched record values can't be edited; the rest of the form stays editable.
- **Printable document:** the "🖨 Print / Save as PDF" button calls `window.print()`; `@media print` in `global.css` hides app chrome (`.df-no-print`, sidebar), shows the branded header + a signature/date line (`.df-print-only`), and flattens inputs into clean document values. Use the browser's "Save as PDF" destination to generate the document.

## Architecture — the big picture

The whole platform is built around **"add a capability = add one bean/descriptor, touch nothing else."** When extending, follow the existing seam rather than special-casing.

- **Field types are a registry + strategy pattern, mirrored on both sides.** Backend: each `FieldType` enum value has a `FieldValidator` bean, all collected into `FieldValidatorRegistry` (`com.docgen.field`). Frontend: `frontend/src/fields/registry.tsx` maps each type to palette metadata, default config, and a renderer. **Adding a field type = enum value + validator bean (backend) + one registry descriptor (frontend).** Nothing else should change.

- **Templates are JSON-schema-driven, not code-per-template.** A `FormTemplate` carries `schemaJson` / `layoutJson` / `validationJson` / `roleAccessJson` as JSONB; `FormField.configJson` holds validation rules, conditional visibility, masking, and calculated expressions. The React renderer interprets these at runtime — there is no per-template React code.

- **Templates are versioned by snapshot.** Publishing serializes the full definition into an immutable `FormTemplateVersion.snapshotJson`, enabling history/rollback.

- **API connectors are plugin-style.** `ApiConnectorClient` (`com.docgen.connector`) resolves an `AuthStrategy` per `AuthType` (NONE/BASIC/BEARER/API_KEY/OAUTH2). Adding an auth scheme = one strategy bean.

- **Prefill flow** (`POST /api/forms/{templateId}/prefill`): load `ApiFieldMapping`s for the template → group by connector → `ApiConnectorClient.execute` (auth strategy + `{{var}}` templating, on virtual threads) → per mapping: JSONPath extract → transform function (`TransformFunctions`) → fallback → required check → return `{ fieldKey: value }` + per-field details + warnings. Rate-limited per template.

- **Security model** (`com.docgen.security`): JWT HS256 (`JwtService`/`JwtAuthFilter`), AES-256-GCM at rest (`CryptoService`, encrypts connector secrets + sensitive submission values), and read-time masking (`MaskingService`, masks SSN/MPI unless caller has `DATA_UNMASK`). Validation is Jakarta Validation on DTOs plus server-side field validation through the strategy registry. `AuditService` records template edits, publishes, clones, mapping changes, and submissions.

- **Multi-tenancy seam:** `organizationId` is already carried on templates, connectors, and submissions; isolation is meant to build on that rather than a new mechanism.

- **Extension points** (`com.docgen.extension`): marker interfaces activated by providing a Spring bean — no core rewrite. `DocumentGenerator` is **implemented** (see below); `WorkflowEngine`, `WebhookPublisher`, `ESignatureProvider`, `RulesEngine`, `DataStandardMapper` (FHIR) remain reserved.

- **Document generation** (`com.docgen.document`): server-side rendering of a submission to a downloadable document — no browser print dialog. Each output format is one `DocumentGenerator` bean (`PdfDocumentGenerator` via OpenPDF, `DocxDocumentGenerator` via Apache POI, `RtfDocumentGenerator` dependency-free). `DocumentModelBuilder` builds a format-neutral `FormDocument` (branding from `schema_json`, fields in order, option codes resolved to labels, sensitive values masked) that every generator renders; `DocumentService` routes by format. Endpoints: `GET /api/documents/formats` and `GET /api/submissions/{id}/document?format=pdf|docx|rtf`. **Adding a format (ODT, XLSX, HTML) = one new `DocumentGenerator` bean** — controller/service/frontend pick it up automatically (the frontend renders a download button per format from `/documents/formats`).

Backend packages map 1:1 to domains: `template`, `field`, `connector`, `prefill`, `submission`, `user`, `audit`, plus cross-cutting `common` (`BaseEntity`, `GlobalExceptionHandler`, `Json`), `security`, and `config`. Schema + seed data live in `backend/src/main/resources/db/migration` (Flyway `V1__init.sql`, `V2__seed.sql`); `ddl-auto` is `validate`, so **schema changes go through a new Flyway migration**, never entity auto-DDL.

Deeper detail (diagrams, data model, REST contracts) lives in `docs/ARCHITECTURE.md` and `docs/API.md`.
