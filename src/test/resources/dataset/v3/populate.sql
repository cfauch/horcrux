 -- apply after schema.sql and before migration scripts.
 -- update versions with their associated migration script.
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(1, NULL);
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(2, 'upgrate_to_v2.sql');
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(3, 'upgrate_to_v3.sql');

