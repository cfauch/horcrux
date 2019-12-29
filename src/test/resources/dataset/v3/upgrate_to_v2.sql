-- apply all changes on database to upgrade it to v2
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);

-- set to true the current version of the database
UPDATE horcrux_versions SET active = TRUE WHERE number = 2;
UPDATE horcrux_versions SET active = FALSE WHERE number != 2;
