-- Seed data so the app is populated and demonstrable on first run.
-- Fixed UUIDs are used for stable references; bcrypt hash below is for the password "password".

-- Default organization
INSERT INTO organization (id, name, slug, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Default Org', 'default', now(), now());

-- Admin user (admin@docgen.local / password). Roles include DATA_UNMASK to view sensitive values.
INSERT INTO app_user (id, email, password_hash, display_name, organization_id, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-0000000000a1', 'admin@docgen.local',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Platform Admin', '00000000-0000-0000-0000-000000000001', now(), now());
INSERT INTO user_role (user_id, role) VALUES
    ('00000000-0000-0000-0000-0000000000a1', 'ADMIN'),
    ('00000000-0000-0000-0000-0000000000a1', 'EDITOR'),
    ('00000000-0000-0000-0000-0000000000a1', 'DATA_UNMASK');

-- ---------------------------------------------------------------------------
-- Template 1: Patient Intake Form
-- ---------------------------------------------------------------------------
INSERT INTO form_template (id, organization_id, name, slug, description, status, category,
                           current_version, schema_json, layout_json, validation_json, role_access_json,
                           created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
     'Patient Intake Form', 'patient-intake',
     'Collects member demographics and contact details for intake.', 'PUBLISHED', 'INTAKE', 2,
     '{"title":"Patient Intake","sections":["Personal","Contact","Address"]}'::jsonb,
     '{"columns":2}'::jsonb,
     '{}'::jsonb,
     '{"view":["VIEWER","EDITOR","ADMIN"],"edit":["EDITOR","ADMIN"]}'::jsonb,
     now(), now());

INSERT INTO form_field (id, template_id, field_key, label, type, required, order_index, config_json, created_at, updated_at) VALUES
    ('1f000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'sectionPersonal', 'Personal Information', 'SECTION_HEADER', false, 0, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'firstName', 'First Name', 'TEXT', true, 1, '{"placeholder":"John","maxLength":80}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'lastName', 'Last Name', 'TEXT', true, 2, '{"placeholder":"Smith","maxLength":80}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 'dob', 'Date of Birth', 'DATE', true, 3, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', 'ssn', 'SSN', 'SSN', true, 4, '{"mask":true}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000001', 'mpi', 'Member ID (MPI)', 'MPI', true, 5, '{"mask":true}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', 'sectionContact', 'Contact', 'SECTION_HEADER', false, 6, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000001', 'email', 'Email', 'EMAIL', true, 7, '{"placeholder":"john@example.com"}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000001', 'phone', 'Phone', 'PHONE', false, 8, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000001', 'addressLine1', 'Address Line 1', 'TEXT', false, 9, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000001', 'addressCity', 'City', 'TEXT', false, 10, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000001', 'addressState', 'State', 'DROPDOWN', false, 11, '{}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000001', 'addressZip', 'ZIP', 'TEXT', false, 12, '{"pattern":"^[0-9]{5}(-[0-9]{4})?$"}'::jsonb, now(), now()),
    ('1f000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000001', 'consent', 'I consent to treatment', 'CHECKBOX', true, 13, '{}'::jsonb, now(), now());

INSERT INTO field_option (id, field_id, value, label, order_index, created_at, updated_at) VALUES
    (gen_random_uuid(), '1f000000-0000-0000-0000-00000000000c', 'NC', 'North Carolina', 0, now(), now()),
    (gen_random_uuid(), '1f000000-0000-0000-0000-00000000000c', 'SC', 'South Carolina', 1, now(), now()),
    (gen_random_uuid(), '1f000000-0000-0000-0000-00000000000c', 'VA', 'Virginia', 2, now(), now()),
    (gen_random_uuid(), '1f000000-0000-0000-0000-00000000000c', 'GA', 'Georgia', 3, now(), now()),
    (gen_random_uuid(), '1f000000-0000-0000-0000-00000000000e', 'yes', 'Yes, I consent', 0, now(), now());

-- ---------------------------------------------------------------------------
-- Template 2: Member Registration
-- ---------------------------------------------------------------------------
INSERT INTO form_template (id, organization_id, name, slug, description, status, category,
                           current_version, schema_json, layout_json, validation_json, role_access_json,
                           created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001',
     'Member Registration', 'member-registration',
     'Registers a new member with eligibility details.', 'DRAFT', 'MEMBER_REGISTRATION', 1,
     '{"title":"Member Registration"}'::jsonb, '{"columns":1}'::jsonb, '{}'::jsonb,
     '{"edit":["EDITOR","ADMIN"]}'::jsonb, now(), now());

INSERT INTO form_field (id, template_id, field_key, label, type, required, order_index, config_json, created_at, updated_at) VALUES
    ('2f000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'firstName', 'First Name', 'TEXT', true, 0, '{}'::jsonb, now(), now()),
    ('2f000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'lastName', 'Last Name', 'TEXT', true, 1, '{}'::jsonb, now(), now()),
    ('2f000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'mpi', 'Member ID', 'MPI', true, 2, '{}'::jsonb, now(), now()),
    ('2f000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', 'planType', 'Plan Type', 'RADIO', true, 3, '{}'::jsonb, now(), now());

INSERT INTO field_option (id, field_id, value, label, order_index, created_at, updated_at) VALUES
    (gen_random_uuid(), '2f000000-0000-0000-0000-000000000004', 'hmo', 'HMO', 0, now(), now()),
    (gen_random_uuid(), '2f000000-0000-0000-0000-000000000004', 'ppo', 'PPO', 1, now(), now());

-- ---------------------------------------------------------------------------
-- Demo connector + prefill mappings for the Intake form.
-- The base URL is illustrative; mappings include fallbacks so prefill still returns
-- demonstrable values even if the external call fails in a sandboxed environment.
-- ---------------------------------------------------------------------------
INSERT INTO api_connector (id, organization_id, name, base_url, http_method, auth_type,
                           headers_json, query_params_json, request_body_template, secret_json,
                           created_at, updated_at) VALUES
    ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
     'Member Directory API', 'https://api.example.com/members/{{memberId}}', 'GET', 'NONE',
     '{"Accept":"application/json"}'::jsonb, '{}'::jsonb, NULL, NULL, now(), now());

INSERT INTO api_field_mapping (id, template_id, connector_id, source, target, transform, fallback_value, required, created_at, updated_at) VALUES
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.firstName', 'firstName', 'capitalize', 'John', true, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.lastName', 'lastName', 'capitalize', 'Smith', true, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.ssn', 'ssn', NULL, '123-45-6789', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.mpi', 'mpi', 'upper', 'MPI12345', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.address.line1', 'addressLine1', NULL, '100 Main St', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.address.city', 'addressCity', 'titleCase', 'Raleigh', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.address.state', 'addressState', 'upper', 'NC', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '$.member.address.zip', 'addressZip', NULL, '27601', false, now(), now());
