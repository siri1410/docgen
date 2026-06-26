# REST API contracts

Base path: `/api`. Interactive docs: `/swagger-ui.html`. OpenAPI JSON: `/v3/api-docs`.

Auth: send `Authorization: Bearer <jwt>` (from `POST /api/auth/login`). The `dev` profile permits
unauthenticated calls for convenience.

## Auth

| Method | Path | Body | Returns |
|--------|------|------|---------|
| POST | `/auth/register` | `{ email, password, displayName? }` | `{ token, userId, email, roles }` |
| POST | `/auth/login` | `{ email, password }` | `{ token, userId, email, roles }` |
| GET  | `/auth/config` | — | JWT settings |

## Templates

| Method | Path | Notes |
|--------|------|-------|
| POST | `/templates` | Create. Body: `{ name, slug?, description?, category?, schema?, layout?, validation?, roleAccess? }` |
| GET  | `/templates` | List summaries for the current org |
| GET  | `/templates/{id}` | Full template incl. fields |
| PUT  | `/templates/{id}` | Update |
| POST | `/templates/{id}/publish` | Snapshots a version. Body: `{ notes? }` |
| POST | `/templates/{id}/clone` | New draft copy. Body: `{ name? }` |
| GET  | `/templates/{id}/versions` | Version history |

## Fields

| Method | Path | Notes |
|--------|------|-------|
| POST   | `/templates/{templateId}/fields` | Add. Body: `{ fieldKey?, label, type, required, orderIndex?, config?, options? }` |
| PUT    | `/templates/{templateId}/fields/{fieldId}` | Update |
| DELETE | `/templates/{templateId}/fields/{fieldId}` | Delete |

`type` is one of the 17 `FieldType` values (see `GET /field-types`).

## Connectors & mappings

| Method | Path | Notes |
|--------|------|-------|
| POST | `/connectors` | Create. Body: `{ name, baseUrl, httpMethod?, authType?, headers?, queryParams?, requestBodyTemplate?, secret? }`. `secret` is encrypted at rest. |
| GET  | `/connectors` | List |
| POST | `/connectors/{id}/test` | Execute a live call. Body: `{ input }` |
| POST | `/templates/{templateId}/mappings` | Replace mappings. Body: `{ mappings: [{ connectorId, source, target, transform?, fallbackValue?, required }] }` |
| GET  | `/templates/{templateId}/mappings` | List mappings |

## Prefill

| Method | Path | Notes |
|--------|------|-------|
| POST | `/forms/{templateId}/prefill` | Body: `{ input }`. Returns `{ values, details[], warnings[] }` |

`input` values fill `{{var}}` placeholders in connector URL/query/body. `source` is a JSONPath into
the response; `transform` is one of `capitalize, upper, lower, trim, titleCase, digitsOnly, ssnMask`.

## Submissions

| Method | Path | Notes |
|--------|------|-------|
| POST | `/forms/{templateId}/submit` | Body: `{ values }`. Server-side validates; encrypts sensitive values. |
| GET  | `/forms/{templateId}/submissions` | List summaries |
| GET  | `/submissions/{id}` | Full submission; sensitive values masked unless `DATA_UNMASK` |

## Metadata & audit

| Method | Path | Notes |
|--------|------|-------|
| GET | `/field-types` | Catalogue of field types + metadata (sensitive/option-backed/presentational) |
| GET | `/audit` | 100 most recent audit entries |

## Example: prefill request/response

```http
POST /api/forms/10000000-0000-0000-0000-000000000001/prefill
Content-Type: application/json

{ "input": { "memberId": "M-1001" } }
```

```json
{
  "values": {
    "firstName": "John", "lastName": "Smith", "ssn": "123-45-6789", "mpi": "MPI12345",
    "addressLine1": "100 Main St", "addressCity": "Raleigh", "addressState": "NC", "addressZip": "27601"
  },
  "details": [
    { "target": "firstName", "value": "John", "source": "$.member.firstName",
      "connector": "Member Directory API", "fromFallback": false }
  ],
  "warnings": []
}
```
