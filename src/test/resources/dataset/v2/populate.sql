/**
 * Apply at database start-up:
 * Updates versions and their associated migration scripts.
 */
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(1, NULL);
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(2, 'upgrate_to_v2.sql');

