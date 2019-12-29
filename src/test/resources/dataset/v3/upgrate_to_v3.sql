-- apply all changes on database to upgrade it to v3
ALTER TABLE horcrux_users ADD email VARCHAR(32);

-- set to true the current version of the database
UPDATE horcrux_versions SET active = TRUE WHERE number = 3;
UPDATE horcrux_versions SET active = FALSE WHERE number != 3;
