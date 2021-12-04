package com.code.fauch.horcrux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;

public final class Migration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);
    private static final String DEFAULT_VERSION_TABLE = "versions";
    private static final String SELECT_SCRIPTS = "SELECT number, script FROM %s WHERE script IS NOT NULL AND number>(SELECT number FROM %s WHERE active IS TRUE) ORDER BY number";
    private static final String ENABLE_VERSION = "UPDATE %s SET active = TRUE WHERE number = ?";
    private static final String DISABLE_OTHER_VERSIONS = "UPDATE %s SET active = FALSE WHERE number != ?";

    /**
     * Name of the table where are store each database versions.
     */
    private final String versionTable;

    /**
     * The SQL command to find all the scripts to apply.
     */
    private final String findScriptsCmd;

    /**
     * The SQL command to enable a version.
     */
    private final String enableVersionCmd;

    /**
     * The SQL command to disable other versions.
     */
    private final String disableOtherVersionsCmd;

    /**
     * Path of the directory containing updates scripts.
     */
    private final Path scriptDir;

    private final boolean createSchema;

    private final boolean runUpdates;
    /**
     * Builder to create and configure <code>DataBase</code> object.
     *
     * @author c.fauch
     *
     */
    public static final class Builder {

        /**
         * The name of table in database where are store all versions
         */
        private String versionTable = null;

        /**
         * Path of the directory where are all SQL script files.
         */
        private Path scripts = null;

        private boolean createSchema = false;

        private boolean runUpdates = false;

        /**
         * Specify the path of the script files directory
         *
         * @param dir the path of the directory (not null)
         * @return this builder
         */
        public Migration.Builder withScripts(final Path dir) {
            this.scripts = Objects.requireNonNull(dir, "dir is mandatory");
            return this;
        }

        /**
         * Specify the name of the table in database where are store all versions.
         *
         * @param name the name of the table (not null)
         * @return this builder
         */
        public Migration.Builder versionTable(final String name) {
            this.versionTable = name;
            return this;
        }

        public Migration.Builder createSchema(final Boolean option) {
            this.createSchema = option;
            return this;
        }

        public Migration.Builder runUpdates(final Boolean option) {
            this.runUpdates = option;
            return this;
        }

        /**
         * Build the <code>Migration</code> object
         *
         * @return the corresponding <code>DataBase</code> instance
         */
        public Migration build() throws URISyntaxException {
            return new Migration(this);
        }

    }

    private Migration(final Builder builder) throws URISyntaxException {
        this.versionTable = builder.versionTable == null ? DEFAULT_VERSION_TABLE : builder.versionTable;
        this.scriptDir = builder.scripts == null ? Paths.get(Objects.requireNonNull(getClass().getResource("/")).toURI()) : builder.scripts;
        this.findScriptsCmd = String.format(SELECT_SCRIPTS, this.versionTable, this.versionTable);
        this.enableVersionCmd = String.format(ENABLE_VERSION, this.versionTable);
        this.disableOtherVersionsCmd = String.format(DISABLE_OTHER_VERSIONS, this.versionTable);
        this.createSchema = builder.createSchema;
        this.runUpdates = builder.runUpdates;
    }

    public void update(final DataSource dataSource) throws SQLException, IOException {
        Connection conn = null;
        try {
            LOGGER.info("opening transactional connection...");
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            initialize(conn);
            runUpdateScripts(conn);
            LOGGER.info("committing transaction...");
            conn.commit();
        } catch (Exception err) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    LOGGER.warn("Unable to rollback updates: {}", e.getMessage());
                }
            }
            throw err;
        } finally {
            if (conn != null) {
                LOGGER.info("closing connection...");
                conn.close();
            }
        }
    }

    private void initialize(final Connection conn) throws SQLException, IOException {
        final DatabaseMetaData meta = conn.getMetaData();
        try(ResultSet tables = meta.getTables(null, null, this.versionTable, null)) {
            if (!tables.next()) {
                if (!this.createSchema) {
                    throw new SQLWarning("Missing table: " + this.versionTable);
                }
                LOGGER.info("creating schema...");
                DataSourceUtils.execute(conn, this.scriptDir.resolve("schema.sql"));
            } else {
                LOGGER.info("populating versions table...");
                DataSourceUtils.execute(conn, this.scriptDir.resolve("populate.sql"));
            }
        }
    }

    private void runUpdateScripts(final Connection conn) throws SQLException, IOException {
        try (PreparedStatement statement = conn.prepareStatement(this.findScriptsCmd)) {
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    if(!this.runUpdates) {
                        throw new SQLWarning("Missing updates.");
                    }
                    do {
                        DataSourceUtils.execute(conn, this.scriptDir.resolve(result.getString(2)));
                        setVersion(conn, result.getInt(1));
                    } while (result.next());
                }
            }
        }
    }

    private void setVersion(final Connection conn, final int version) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(this.enableVersionCmd)) {
            statement.setInt(1, version);
            statement.execute();
        }
        try (PreparedStatement statement = conn.prepareStatement(this.disableOtherVersionsCmd)) {
            statement.setInt(1, version);
            statement.execute();
        }
    }

}
