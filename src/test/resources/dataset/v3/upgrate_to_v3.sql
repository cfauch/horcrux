-- apply all changes on database to upgrade it to v3
ALTER TABLE HORCRUX_USERS ADD email VARCHAR(32);

-- set to true the current version of the database
UPDATE HORCRUX_VERSIONS SET active = TRUE WHERE number = 3;
UPDATE HORCRUX_VERSIONS SET active = FALSE WHERE number != 3;
