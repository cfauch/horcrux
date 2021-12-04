package com.code.fauch.horcrux;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MigrationTest {

    @TempDir
    public Path folder;

    @Test
    public void testEmptyBaseFirstVersion() throws URISyntaxException, SQLException, IOException {
        final DataSource ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v1;DB_CLOSE_DELAY=1",
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v1").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(1, getCurrentVersion(ds));
    }

    @Test
    public void testEmptyBaseFirstVersionWithoutSchemaCreation() throws URISyntaxException, SQLException, IOException {
        final DataSource ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v2;DB_CLOSE_DELAY=1",
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(false)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v1").toURI()))
                .build();
        Assertions.assertThrows(SQLWarning.class, ()->migration.update(ds));
    }

    @Test
    public void testEmptyBaseV2() throws URISyntaxException, SQLException, IOException {
        final DataSource ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v3;DB_CLOSE_DELAY=1",
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v2").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(2, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE");
    }

    @Test
    public void testOldExistingBaseWithoutUpgrade() throws URISyntaxException, SQLException, IOException {
        final URI dbFile = getClass().getResource("/dataset/v1/").toURI().resolve("v1");
        final String url = String.format("jdbc:h2:%s", dbFile.getPath());
        final DataSource ds = JdbcConnectionPool.create(
                url,
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(false)
                .withScripts(Paths.get(getClass().getResource("/dataset/v2").toURI()))
                .build();
        Assertions.assertThrows(SQLWarning.class, ()->migration.update(ds));
    }

    @Test
    public void testV1ToV2Upgrade() throws URISyntaxException, SQLException, IOException {
        final Path dbFile = this.folder.resolve("yo.mv.db");
        Files.copy(Paths.get(getClass().getResource("/dataset/v1/v1.mv.db").toURI()), dbFile);
        final String url = String.format("jdbc:h2:%s", this.folder.resolve("yo"));
        final DataSource ds = JdbcConnectionPool.create(
                url,
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v2").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(2, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE");
    }

    @Test
    public void testEmptyBaseV3() throws URISyntaxException, SQLException, IOException {
        final DataSource ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v4;DB_CLOSE_DELAY=1",
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v3").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(3, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE", "EMAIL");
    }

    @Test
    public void testV1ToV3Upgrade() throws URISyntaxException, SQLException, IOException {
        final Path dbFile = this.folder.resolve("yo.mv.db");
        Files.copy(Paths.get(getClass().getResource("/dataset/v1/v1.mv.db").toURI()), dbFile);
        final String url = String.format("jdbc:h2:%s", this.folder.resolve("yo"));
        final DataSource ds = JdbcConnectionPool.create(
                url,
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v3").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(3, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE", "EMAIL");
    }

    @Test
    public void testV2ToV3Upgrade() throws URISyntaxException, SQLException, IOException {
        final Path dbFile = this.folder.resolve("yo.mv.db");
        Files.copy(Paths.get(getClass().getResource("/dataset/v2/v2.mv.db").toURI()), dbFile);
        final String url = String.format("jdbc:h2:%s", this.folder.resolve("yo"));
        final DataSource ds = JdbcConnectionPool.create(
                url,
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v3").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(3, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE", "EMAIL");
    }

    @Test
    public void testV1UpgradedV2ToV3Upgrade() throws URISyntaxException, SQLException, IOException {
        final Path dbFile = this.folder.resolve("yo.mv.db");
        Files.copy(Paths.get(getClass().getResource("/dataset/v2/v1_to_v2.mv.db").toURI()), dbFile);
        final String url = String.format("jdbc:h2:%s", this.folder.resolve("yo"));
        final DataSource ds = JdbcConnectionPool.create(
                url,
                "harry",
                ""
        );
        final Migration migration = new Migration.Builder()
                .versionTable("horcrux_versions".toUpperCase())
                .createSchema(true)
                .runUpdates(true)
                .withScripts(Paths.get(getClass().getResource("/dataset/v3").toURI()))
                .build();
        migration.update(ds);
        Assertions.assertEquals(3, getCurrentVersion(ds));
        checkTable(ds, "horcrux_users".toUpperCase(), "ID", "NAME", "PROFILE", "EMAIL");
    }

    private static int getCurrentVersion(final DataSource ds) throws SQLException {
        try(Connection conn = ds.getConnection()) {
            try(PreparedStatement statement = conn.prepareStatement("SELECT number FROM horcrux_versions WHERE active IS TRUE")) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                    return -1;
                }
            }
        }
    }

    private static void checkTable(final DataSource ds, final String table, final String... columns) throws SQLException {
        try(Connection conn = ds.getConnection()) {
            final DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet found = meta.getTables(null, null, table, null)) {
                Assertions.assertTrue(found.next());
            }
            try(ResultSet found = meta.getColumns(null, null, table, null)) {
                final ArrayList<String> colNames = new ArrayList<>();
                while (found.next()) {
                    colNames.add(found.getString("COLUMN_NAME"));
                }
                Assertions.assertEquals(Arrays.asList(columns), colNames);
            }
        }
    }

}
