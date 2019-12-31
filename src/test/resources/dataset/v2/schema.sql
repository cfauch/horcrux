-- apply before all other scripts.
CREATE TABLE IF NOT EXISTS HORCRUX_VERSIONS (
    number INT NOT NULL,
    script VARCHAR(64),
    active BOOLEAN
);

-- set to true the current version of the database
MERGE INTO HORCRUX_VERSIONS (number, script, active) KEY(number) VALUES(2, NULL, true);

-- do other tables and constraints creation
CREATE TABLE IF NOT EXISTS HORCRUX_USERS (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);
