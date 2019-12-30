# horcrux
How to manage database upgrade.

## Installation

If you use Maven add this dependency:

```
    <dependency>
      <groupId>com.fauch.code</groupId>
      <artifactId>horcrux</artifactId>
      <version>1.0.0</version>
    </dependency>
```

Then choose the connection pool you want to use and add the associated plugin:

```
   <dependency>
      <groupId>com.fauch.code</groupId>
      <artifactId>horcrux-hikari</artifactId>
      <version>1.0.0</version>
    </dependency>
```

And finally choose the JDBC driver for the database you want to use:

```
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.2.7</version>
    </dependency>
```

## Manage database upgrade with horcrux

Horcrux use SQL script files to create, populate and upgrade database.

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
This script will be executed before any other operations on the database if it is empty.

#### 2. Create a `populate.sql` file under this directory with this content:

```
INSERT INTO horcrux_versions (number, script) VALUES 
    (1 , NULL)
ON CONFLICT (number) DO UPDATE SET script = EXCLUDED.script 
WHERE horcrux_versions.number = EXCLUDED.number;
```
This script will be used to update all knowing versions of the database.

#### 3. Create a `Main.java` class like this:

```
public static void main(String[] args) throws Exception {
    final Properties prop = new Properties();
    prop.setProperty("jdbcUrl", "jdbc:postgresql:hx");
    prop.setProperty("username", "totoro");
    prop.setProperty("password", "2what4?");
    prop.setProperty("autoCommit", "false");
    prop.setProperty("poolName", "database-connection-pool");
    final Path scripts = Paths.get(Main.class.getResource("/database").toURI());
    try(DataBase db = DataBase.init("pool")
                        .withScripts(scripts)
                        .versionTable("horcrux_versions")
                        .build(prop)) {
        db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
        System.out.println("version:" + db.getCurrentVersion());
    }
}
```
- `Properties` configure the [HikariCP](https://github.com/brettwooldridge/HikariCP) data source.
- `DataBase` is the main object used to access to the database.
- `withScrips` Specifies the directory path where the SQL scripts are located
- `verstionTable` Specifies the name of the table where the versions of the database are located
- `db.open()` will prepare the database
- `ECreateOption.SCHEMA` indicates that `schema.sql` must be applied if the base is empty.
- `ECreateOption.UPGRADE` indicates that migration scripts should be applied if the database needs to be upgraded.

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

### Database upgrading

Imagine that we want to add a new table named `horcrux_users`.

#### 1. Edit `schema.sql`:
- Change the version to 2: 

```
INSERT INTO horcrux_versions (number, script, active) VALUES (2 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number
```
- Create the new table:

```
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
    email VARCHAR(32)
);
```
- the content of the file must be now:

```
CREATE TABLE IF NOT EXISTS horcrux_versions (
    number INT NOT NULL UNIQUE,
    script VARCHAR(64),
    active BOOLEAN
);
INSERT INTO horcrux_versions (number, script, active) VALUES (2 , NULL, true)
ON CONFLICT (number) DO UPDATE
    SET script = EXCLUDED.script, active=true
    WHERE horcrux_versions.number=EXCLUDED.number;
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
    email VARCHAR(32)
);
```

#### 2. Edit `populate.sql`:

Add the new version and the corresponding script to apply: `(2, 'upgrate_to_v2.sql')`

The content of the file must be now:

```
INSERT INTO horcrux_versions (number, script) VALUES 
    (1 , NULL),
    (2, 'upgrate_to_v2.sql')
ON CONFLICT (number) DO UPDATE SET script = EXCLUDED.script 
WHERE horcrux_versions.number = EXCLUDED.number;
```

#### 3. Create a `upgrade_to_v2.sql` file under the same directory:

- Create the new table

```
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);
```

- set the current version of the database to true

```
UPDATE horcrux_versions SET active = TRUE WHERE number = 2;
UPDATE horcrux_versions SET active = FALSE WHERE number != 2;
```

- the content of this file look like this now:

```
CREATE TABLE IF NOT EXISTS horcrux_users (
    id UUID PRIMARY KEY,
    name VARCHAR(64),
    profile VARCHAR(32)
);
UPDATE horcrux_versions SET active = TRUE WHERE number = 2;
UPDATE horcrux_versions SET active = FALSE WHERE number != 2;
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
