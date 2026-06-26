-- Doc4j-style Form Builder — initial schema.
-- JSONB columns hold extensible schema/layout/validation/config so new capabilities
-- can be added without further migrations.

CREATE TABLE organization (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

CREATE TABLE app_user (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255),
    organization_id UUID NOT NULL,
    created_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role    VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE form_template (
    id               UUID PRIMARY KEY,
    organization_id  UUID NOT NULL,
    name             VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL,
    description      TEXT,
    status           VARCHAR(32) NOT NULL,
    category         VARCHAR(64),
    current_version  INT NOT NULL,
    schema_json      JSONB,
    layout_json      JSONB,
    validation_json  JSONB,
    role_access_json JSONB,
    created_at       TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ
);
CREATE INDEX idx_template_org ON form_template(organization_id);

CREATE TABLE form_field (
    id          UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    field_key   VARCHAR(255) NOT NULL,
    label       VARCHAR(255) NOT NULL,
    type        VARCHAR(32) NOT NULL,
    required    BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL DEFAULT 0,
    config_json JSONB,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);
CREATE INDEX idx_field_template ON form_field(template_id);

CREATE TABLE field_option (
    id          UUID PRIMARY KEY,
    field_id    UUID NOT NULL REFERENCES form_field(id) ON DELETE CASCADE,
    value       VARCHAR(255) NOT NULL,
    label       VARCHAR(255) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);
CREATE INDEX idx_option_field ON field_option(field_id);

CREATE TABLE form_template_version (
    id             UUID PRIMARY KEY,
    template_id    UUID NOT NULL,
    version_number INT NOT NULL,
    published_by   VARCHAR(255),
    notes          TEXT,
    snapshot_json  JSONB NOT NULL,
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ
);
CREATE INDEX idx_version_template ON form_template_version(template_id);

CREATE TABLE api_connector (
    id                    UUID PRIMARY KEY,
    organization_id       UUID NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    base_url              VARCHAR(1024) NOT NULL,
    http_method           VARCHAR(16) NOT NULL,
    auth_type             VARCHAR(32) NOT NULL,
    headers_json          JSONB,
    query_params_json     JSONB,
    request_body_template TEXT,
    secret_json           TEXT,
    created_at            TIMESTAMPTZ,
    updated_at            TIMESTAMPTZ
);
CREATE INDEX idx_connector_org ON api_connector(organization_id);

CREATE TABLE api_field_mapping (
    id             UUID PRIMARY KEY,
    template_id    UUID NOT NULL,
    connector_id   UUID NOT NULL,
    source         VARCHAR(512) NOT NULL,
    target         VARCHAR(255) NOT NULL,
    transform      VARCHAR(64),
    fallback_value VARCHAR(512),
    required       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ
);
CREATE INDEX idx_mapping_template ON api_field_mapping(template_id);

CREATE TABLE form_submission (
    id               UUID PRIMARY KEY,
    template_id      UUID NOT NULL,
    organization_id  UUID NOT NULL,
    submitted_by     VARCHAR(255),
    template_version INT,
    created_at       TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ
);
CREATE INDEX idx_submission_template ON form_submission(template_id);

CREATE TABLE form_submission_value (
    id            UUID PRIMARY KEY,
    submission_id UUID NOT NULL REFERENCES form_submission(id) ON DELETE CASCADE,
    field_key     VARCHAR(255) NOT NULL,
    value_json    TEXT,
    sensitive     BOOLEAN NOT NULL DEFAULT FALSE,
    encrypted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ
);
CREATE INDEX idx_value_submission ON form_submission_value(submission_id);

CREATE TABLE audit_log (
    id          UUID PRIMARY KEY,
    actor       VARCHAR(255) NOT NULL,
    action      VARCHAR(128) NOT NULL,
    entity_type VARCHAR(128) NOT NULL,
    entity_id   UUID,
    detail_json JSONB,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);
CREATE INDEX idx_audit_created ON audit_log(created_at DESC);
