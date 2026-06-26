-- ---------------------------------------------------------------------------
-- Template 3: Driving License Renewal  (government / state DMV sample)
--
-- Demonstrates the core docgen value prop for agency forms: some details are
-- STATIC (defined on the template — renewal reason, organ-donor choice,
-- certification) while others are FED IN dynamically from a DMV record lookup
-- keyed on the applicant's input (the Driver License Number).
--
-- Category is the free-form string 'DRIVING_LICENSE' — no enum/schema change
-- was needed to onboard a brand-new government form type.
-- ---------------------------------------------------------------------------
INSERT INTO form_template (id, organization_id, name, slug, description, status, category,
                           current_version, schema_json, layout_json, validation_json, role_access_json,
                           created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001',
     'Driving License Renewal', 'driving-license-renewal',
     'State DMV driver license renewal. Looks up the holder record by license number and prefills it.',
     'PUBLISHED', 'DRIVING_LICENSE', 1,
     '{"title":"Driving License Renewal","agency":"State DMV","sections":["License Lookup","DMV Record","Renewal Details","Certification"]}'::jsonb,
     '{"columns":2}'::jsonb,
     '{}'::jsonb,
     '{"view":["VIEWER","EDITOR","ADMIN"],"edit":["EDITOR","ADMIN"]}'::jsonb,
     now(), now());

INSERT INTO form_field (id, template_id, field_key, label, type, required, order_index, config_json, created_at, updated_at) VALUES
    -- Section 1: the applicant's input (the lookup key) -----------------------
    ('3f000000-0000-0000-0000-000000000000', '10000000-0000-0000-0000-000000000003', 'sectionLookup', 'License Lookup', 'SECTION_HEADER', false, 0, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'dlNumber', 'Driver License Number', 'TEXT', true, 1, '{"placeholder":"NC-D1234567","help":"Enter your license number; we will retrieve your record."}'::jsonb, now(), now()),
    -- Section 2: DMV record — DYNAMIC, populated by the prefill lookup --------
    ('3f000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'sectionRecord', 'DMV Record (auto-filled)', 'SECTION_HEADER', false, 2, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'verifiedLicenseNumber', 'Verified License Number', 'TEXT', false, 3, '{"autofill":true,"readOnly":true,"help":"Echoed back from the live DMV lookup to confirm the key matched."}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'firstName', 'First Name', 'TEXT', true, 4, '{"autofill":true}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003', 'lastName', 'Last Name', 'TEXT', true, 5, '{"autofill":true}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000003', 'dob', 'Date of Birth', 'DATE', true, 6, '{"autofill":true}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000003', 'licenseClass', 'License Class', 'TEXT', false, 7, '{"autofill":true}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000003', 'currentAddress', 'Current Address on File', 'TEXT', false, 8, '{"autofill":true}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000003', 'expirationDate', 'Current Expiration Date', 'DATE', false, 9, '{"autofill":true}'::jsonb, now(), now()),
    -- Section 3: renewal choices — STATIC, defined entirely on the template ---
    ('3f000000-0000-0000-0000-00000000000a', '10000000-0000-0000-0000-000000000003', 'sectionRenewal', 'Renewal Details', 'SECTION_HEADER', false, 10, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-00000000000b', '10000000-0000-0000-0000-000000000003', 'renewalReason', 'Reason for Renewal', 'DROPDOWN', true, 11, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-00000000000c', '10000000-0000-0000-0000-000000000003', 'addressChanged', 'Has your address changed?', 'CHECKBOX', false, 12, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-00000000000d', '10000000-0000-0000-0000-000000000003', 'organDonor', 'Register as an organ donor?', 'RADIO', true, 13, '{}'::jsonb, now(), now()),
    -- Section 4: certification — STATIC consent ------------------------------
    ('3f000000-0000-0000-0000-00000000000e', '10000000-0000-0000-0000-000000000003', 'sectionCertify', 'Certification', 'SECTION_HEADER', false, 14, '{}'::jsonb, now(), now()),
    ('3f000000-0000-0000-0000-00000000000f', '10000000-0000-0000-0000-000000000003', 'certify', 'I certify this information is accurate', 'CHECKBOX', true, 15, '{}'::jsonb, now(), now());

INSERT INTO field_option (id, field_id, value, label, order_index, created_at, updated_at) VALUES
    -- renewalReason (DROPDOWN)
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000b', 'standard', 'Standard renewal', 0, now(), now()),
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000b', 'address',  'Address change',   1, now(), now()),
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000b', 'name',     'Name change',      2, now(), now()),
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000b', 'replace',  'Replace lost or damaged license', 3, now(), now()),
    -- addressChanged (CHECKBOX)
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000c', 'yes', 'Yes, update my address', 0, now(), now()),
    -- organDonor (RADIO)
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000d', 'yes', 'Yes', 0, now(), now()),
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000d', 'no',  'No',  1, now(), now()),
    -- certify (CHECKBOX)
    (gen_random_uuid(), '3f000000-0000-0000-0000-00000000000f', 'yes', 'I certify the above is true and accurate', 0, now(), now());

-- ---------------------------------------------------------------------------
-- DMV lookup connector + mappings.
-- The base URL templates the applicant's {{dlNumber}} input into a live call.
-- '$.args.dl' is echoed back by the endpoint, proving the INPUT drives the
-- prefilled value end-to-end (fromFallback=false). The remaining record fields
-- map from a DMV-record JSON shape and fall back to representative values when
-- the live record isn't available in a sandbox (fromFallback=true) — exactly
-- how a real DMV connector response ($.record.*) would populate them.
-- ---------------------------------------------------------------------------
INSERT INTO api_connector (id, organization_id, name, base_url, http_method, auth_type,
                           headers_json, query_params_json, request_body_template, secret_json,
                           created_at, updated_at) VALUES
    ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001',
     'State DMV Record Lookup', 'https://postman-echo.com/get', 'GET', 'NONE',
     '{"Accept":"application/json"}'::jsonb, '{"dl":"{{dlNumber}}"}'::jsonb, NULL, NULL, now(), now());

INSERT INTO api_field_mapping (id, template_id, connector_id, source, target, transform, fallback_value, required, created_at, updated_at) VALUES
    -- LIVE: echoes the applicant's input back through a real HTTP round-trip.
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.args.dl', 'verifiedLicenseNumber', 'upper', NULL, true, now(), now()),
    -- DMV record fields (live $.record.* when available; representative fallback otherwise).
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.firstName',      'firstName',      'capitalize', 'Jordan', true,  now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.lastName',       'lastName',       'capitalize', 'Carter', true,  now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.dob',            'dob',            NULL,         '1990-04-12', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.licenseClass',   'licenseClass',   'upper',      'C', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.address',        'currentAddress', 'titleCase',  '742 Evergreen Terrace, Raleigh, NC 27601', false, now(), now()),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '$.record.expirationDate', 'expirationDate', NULL,         '2026-08-31', false, now(), now());
