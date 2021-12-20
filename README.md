# horcrux
How to manage database versions.

## Installation

If you use Maven add this dependency:

```
    <dependency>
      <groupId>com.fauch.code</groupId>
      <artifactId>horcrux</artifactId>
      <version>1.0.1</version>
    </dependency>
```

Then choose the JDBC driver for the database you want to use:

```
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.2.7</version>
    </dependency>
```

In case of `module.info` you have to import the module with the line:
`requires com.code.fauch.horcrux` and optionally if you use postgresql driver
you have also to import this module: `requires org.postgresql.jdbc`.
Here is the module-info used for the following example:

```
module test {
    requires com.code.fauch.revealer;
    requires org.postgresql.jdbc;
}
```

## Database versioning with horcrux

Horcrux use SQL script files to create, populate and update database.

Sample SQL scripts are present under `doc/` directory

### Setting up

Create a directory under `src/main/resources` (ex: `src/main/resources/database`). 

#### 1. Create a `schema.sql` file under this directory with this content:

```
CREATE TABLE IF NOT EXISTS horcrux_versions (
    number INT NOT NULL UNIQUE,
    script VARCHAR(64),
    active BOOLEAN
);

INSERT INTO horcrux_versions (number, script, active) VALUES (1 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number;
```
This script will be executed if database is empty.

#### 2. Create a `Main.java` class like this:

```
   public static void main(String[] args) throws URISyntaxException, SQLException, IOException {
        final PGSimpleDataSource source = new PGSimpleDataSource();
        source.setUrl("jdbc:postgresql:hx");
        source.setUser("toto");
        source.setPassword("yolo?");
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions")
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(Main.class.getResource("/database").toURI()))
                .build();
        migration.update(source);
    }
```
- `source` the PostgreSQL data source.
- `withScrips` specifies the directory path where the SQL scripts are located
- `createSchema(true)` specifies to apply the script `schema.sql` if the database is empty
- `runUpdates` specifies to apply migration scripts if the database is too old.
- `verstionTable` Specifies the name of the table where the versions of the database are located
- `migration.update(source)` Checks and updates the given data source.

#### 4. Run and check...

- Tables:

```
hx=> \dt
               Liste des relations
 Schéma |       Nom        | Type  | Propriétaire 
--------+------------------+-------+--------------
 public | horcrux_versions | table | totoro
(1 ligne)
```

- Versions:

```
hx=> select * from horcrux_versions;
 number | script | active 
--------+--------+--------
      1 |        | t
(1 ligne)
```

### Database updating

Imagine that we want to add a new table named `horcrux_users`.

#### 1. Edit `schema.sql`:

- Create the new table:

```
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
    email VARCHAR(32)
);
```
- Change the version to 2:

```
INSERT INTO horcrux_versions (number, script, active) VALUES (2 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number
```

- the content of the file must be now:

```
CREATE TABLE IF NOT EXISTS horcrux_versions (
    number INT NOT NULL UNIQUE,
    script VARCHAR(64),
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
    email VARCHAR(32)
);

INSERT INTO horcrux_versions (number, script, active) VALUES (2 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number;
```

#### 2. create `populate.sql`:

Create `populate.sql` file under `src/main/resources/database`.
This file will be taken into account if the database is not empty.
It contains all the known versions of the database. So write the following command:
```
INSERT INTO horcrux_versions (number, script) VALUES
    (2, 'upgrate_to_v2.sql')
ON CONFLICT (number) DO UPDATE SET script = EXCLUDED.script 
WHERE horcrux_versions.number = EXCLUDED.number;
```
This command allows you to add a new version (`2`) with the name of the migration
script (`'upgrate_to_v2.sql'`) to apply to update the database.

#### 3. Create a `upgrade_to_v2.sql`:

Create `upgrade_to_v2.sql` file under `src/main/resources/database`
with the following content:

```
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);
```

#### 4. Run and check...

- Tables:

```
hx=> \dt
               Liste des relations
 Schéma |       Nom        | Type  | Propriétaire 
--------+------------------+-------+--------------
 public | horcrux_users    | table | totoro
 public | horcrux_versions | table | totoro
(2 lignes)
```

- Versions:

```
hx=> select * from horcrux_versions;
 number |      script       | active 
--------+-------------------+--------
      2 | upgrate_to_v2.sql | t
      1 |                   | f
(2 lignes)
```
