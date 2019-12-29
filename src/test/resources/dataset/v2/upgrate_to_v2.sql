-- apply all changes on database


-- set to true the current version of the database
UPDATE horcrux_versions SET active = TRUE WHERE number = 2;
UPDATE horcrux_versions SET active = FALSE WHERE number != 2;
