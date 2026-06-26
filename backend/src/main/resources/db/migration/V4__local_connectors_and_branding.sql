-- ---------------------------------------------------------------------------
-- V4: point demo connectors at the LOCAL mock directory (no external network),
-- and add department branding (logo + heading) to the templates so generated
-- documents carry an official header.
-- ---------------------------------------------------------------------------

-- 1) Repoint the Member Directory connector from the dead external host to the
--    local in-process mock. This removes the
--    "I/O error on GET ... https://api.example.com/members/..." prefill warning.
UPDATE api_connector
   SET base_url   = 'http://localhost:8080/api/mock/members/{{memberId}}',
       updated_at = now()
 WHERE id = '30000000-0000-0000-0000-000000000001';

-- 2) Repoint the State DMV connector at the local mock as well (path-based id),
--    so the Driving License form prefills fully offline. The mock echoes the id
--    under $.args.dl and returns the record under $.record.*, matching the mappings.
UPDATE api_connector
   SET base_url          = 'http://localhost:8080/api/mock/dmv/{{dlNumber}}',
       query_params_json = '{}'::jsonb,
       updated_at        = now()
 WHERE id = '30000000-0000-0000-0000-000000000002';

-- 3) Department branding. The frontend document header reads schema.department /
--    schema.logo / schema.lookup; mappings stay untouched. ('||' merges top-level keys.)
UPDATE form_template
   SET schema_json = schema_json || '{"department":"Department of Health Services","logo":"🏥","lookup":{"key":"memberId","label":"Member ID","placeholder":"M-1001"}}'::jsonb,
       updated_at = now()
 WHERE id = '10000000-0000-0000-0000-000000000001';

UPDATE form_template
   SET schema_json = schema_json || '{"department":"Department of Motor Vehicles","logo":"🚗","lookup":{"key":"dlNumber","label":"Driver License Number","placeholder":"NC-D1234567"}}'::jsonb,
       updated_at = now()
 WHERE id = '10000000-0000-0000-0000-000000000003';

UPDATE form_template
   SET schema_json = COALESCE(schema_json, '{}'::jsonb) || '{"department":"Member Services","logo":"🪪","lookup":{"key":"memberId","label":"Member ID","placeholder":"M-1001"}}'::jsonb,
       updated_at = now()
 WHERE id = '10000000-0000-0000-0000-000000000002';
