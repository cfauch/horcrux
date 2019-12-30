-- apply after schema.sql and before migration scripts.
-- update versions with their associated migration script.
INSERT INTO horcrux_versions (number, script) VALUES 
    (1 , NULL) 
--    (2, 'upgrate_to_v2.sql'),
--    (3, 'upgrate_to_v3.sql')
ON CONFLICT (number) DO UPDATE SET script = EXCLUDED.script 
WHERE horcrux_versions.number = EXCLUDED.number;
