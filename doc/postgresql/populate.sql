 -- applied when database already exists, just before migration scripts.
 -- known versions with their associated migration script.
INSERT INTO horcrux_versions (number, script) VALUES
    (2, 'upgrate_to_v2.sql')
ON CONFLICT (number) DO UPDATE SET script = EXCLUDED.script
WHERE horcrux_versions.number = EXCLUDED.number;
