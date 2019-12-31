-- apply after schema.sql and before migration scripts.
-- update versions with their associated migration script.
MERGE INTO HORCRUX_VERSIONS (number, script) KEY(number) VALUES(1, NULL);
--MERGE INTO HORCRUX_VERSIONS (number, script) KEY(number) VALUES(2, 'upgrate_to_v2.sql');

