-- apply before all other scripts.
CREATE TABLE IF NOT EXISTS horcrux_versions (
	number INT NOT NULL UNIQUE,
	script VARCHAR(64),
	active BOOLEAN
);

-- set to true the current version of the database (replace 1 with the current version of database)
INSERT INTO horcrux_versions (number, script, active) VALUES (1 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number;

-- do other tables and constraints creation
--CREATE TABLE IF NOT EXISTS horcrux_users (
--    id UUID PRIMARY KEY,
--    name VARCHAR(64),
--    profile VARCHAR(32)
--    email VARCHAR(32)
--);
