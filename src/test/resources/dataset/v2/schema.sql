/**
 * Apply at database start-up when it is just created:
 * Creates all table and initialize active database version.
 */
CREATE TABLE IF NOT EXISTS horcrux_versions (
	number INT NOT NULL,
	script VARCHAR(64),
	active BOOLEAN
);

-- set to true the current version of the database
MERGE INTO horcrux_versions (number, script, active) KEY(number) VALUES(2, NULL, true);


-- do other tables and contraints creation
