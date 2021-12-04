-- apply all changes on database to upgrade it to v2
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);
