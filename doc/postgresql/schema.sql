-- applied only if database is empty.
-- creates schema, insert predefined records
CREATE TABLE IF NOT EXISTS horcrux_versions (
    number INT NOT NULL UNIQUE,
    script VARCHAR(64),
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
    email VARCHAR(32)
);

-- updates the current version of the database
INSERT INTO horcrux_versions (number, script, active) VALUES (2 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number;

