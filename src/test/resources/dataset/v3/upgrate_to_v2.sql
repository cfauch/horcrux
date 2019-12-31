-- apply all changes on database to upgrade it to v2
CREATE TABLE IF NOT EXISTS HORCRUX_USERS (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);

-- set to true the current version of the database
UPDATE HORCRUX_VERSIONS SET active = TRUE WHERE number = 2;
UPDATE HORCRUX_VERSIONS SET active = FALSE WHERE number != 2;
