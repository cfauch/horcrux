 -- update versions with their associated migration script.
MERGE INTO horcrux_versions (number, script) KEY(number) VALUES(1, NULL);


-- fill other tables with other data
