/*
 * Copyright 2019 Claire Fauch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.code.fauch.horcrux;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import com.code.fauch.horcrux.spi.IHorcrux;
import com.code.fauch.horcrux.spi.Providers;

/**
 * Main facade object to manage database.
 * 
 * @author c.fauch
 *
 */
public final class DataBase implements AutoCloseable {
    
    private static final String DEFAULT_VERSION_TABLE = "versions";
    
    private static final String ACTIVE_VERSION = "SELECT number FROM %s WHERE active IS TRUE";
    private static final String SELECT_SCRIPTS = "SELECT script FROM %s WHERE script IS NOT NULL AND number>(SELECT number FROM %s WHERE active IS TRUE) ORDER BY number";
    
    /**
     * Name of the table where are store each database versions.
     */
    private final String versionTable;
    
    /**
     * The SQL command to get the current version of the database
     */
    private final String activeVersionCmd;

    /**
     * The SQL command to find all the scripts to apply.
     */
    private final String findScriptsCmd;

    /**
     * Path of the scripts directory.
     */
    private final Path scriptDir;
    
    /**
     * The horcrux ;)
     * It know how to create and close data source.
     */
    private final IHorcrux horcrux;
    
    /**
     * The data source.
     */
    private final DataSource ds;
    
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
    
        /***
         * The horcrux used to manage data source.
         */
        private final IHorcrux horcrux;
        
        /**
         * Constrcutor.
         * 
         * @param horcrux the horcrux (not null)
         */
        private Builder(final IHorcrux horcrux) {
            this.horcrux = horcrux;
        }
        
        /**
         * Specify the path of the script files directory
         * 
         * @param dir the path of the directory (not null)
         * @return this builder
         */
        public Builder withScripts(final Path dir) {
            this.scripts = dir;
            return this;
        }
        
        /**
         * Specify the name of the table in database where are store all versions.
         * 
         * @param name the name of the table (not null)
         * @return this builder
         */
        public Builder versionTable(final String name) {
            this.versionTable = name;
            return this;
        }
        
        /**
         * Build the <code>DataBase</code> object with the given data source properties.
         * 
         * @param prop data source properties
         * @return the corresponding <code>DataBase</code> instance
         * @throws URISyntaxException
         */
        public DataBase build(final Properties prop) throws URISyntaxException {
            return new DataBase(this, prop);
        }
    }
    
    /**
     * Constructor.
     * 
     * @param builder the builder (not null)
     * @param props the data source properties (not null)
     * @throws URISyntaxException
     */
    private DataBase(final Builder builder, final Properties props) throws URISyntaxException {
        this.versionTable = builder.versionTable == null ? DEFAULT_VERSION_TABLE : builder.versionTable;
        this.scriptDir = builder.scripts == null ? Paths.get(getClass().getResource("/").toURI()) : builder.scripts;
        this.activeVersionCmd = String.format(ACTIVE_VERSION, this.versionTable);
        this.findScriptsCmd = String.format(SELECT_SCRIPTS, this.versionTable, this.versionTable);
        this.horcrux = builder.horcrux;
        this.ds = this.horcrux.newDataSource(props);
    }
    
    /**
     * Creates and returns a <code>DataBase</code> builder with the forcrux corresponding to the
     * given type.
     * 
     * @param type the type of the requested horcrux
     * @return
     */
    public static Builder init(final String type) {
        return new Builder(Providers.getHorcruxInstance(type));
    }
    
    /**
     * Open and initialize data base with the given options.
     * If the versions table is not found and SCHEMA option is not given a SQL warning is raised. 
     * Otherwise, the schema.sql script is run to create the tables of the database, before to run the 
     * populate.sql script to populate data base with data. 
     * If the database is to old and UPGRADE option is not given, an exception is raised.
     * Otherwise, apply all upgrade scripts. 
     * 
     * @return returns the current version of the database or null if not found (it's probably an error)
     * @throws SQLException
     * @throws IOException
     */
    public Integer open(final ECreateOption... options) throws SQLException, IOException {
        boolean applySchema = false;
        boolean applyUpgrade = false;
        final List<String> scripts = new ArrayList<>();
        
        for (ECreateOption opt : options) {
            if (opt == ECreateOption.SCHEMA) {
                applySchema = true;
            } else if (opt == ECreateOption.UPGRADE) {
                applyUpgrade = true;
            }
        }
        
        try (Connection conn = openSession()) {
            final DatabaseMetaData meta = conn.getMetaData();
            final ResultSet tables = meta.getTables(null, null, this.versionTable.toUpperCase(), null);
            if (tables.next()) {
                execute(conn, this.scriptDir.resolve("populate.sql"));
            }
            else if (applySchema) {
                execute(conn, this.scriptDir.resolve("schema.sql"));
                execute(conn, this.scriptDir.resolve("populate.sql"));
            } else {
                throw new SQLWarning("The version table is not present in the current database: " + this.versionTable);
            }
            
            try (PreparedStatement statement = conn.prepareStatement(this.findScriptsCmd)) {
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        scripts.add(result.getString(1));
                    }
                }
            }
            if (!scripts.isEmpty()) {
                if (applyUpgrade) {
                    for (String script : scripts) {
                        System.out.println("apply script " + script);
                        execute(conn, this.scriptDir.resolve(script));
                    }
                } else {
                    throw new SQLWarning("The current database should be upgraded.");
                }
            }

            conn.commit();
            
            try (PreparedStatement statement = conn.prepareStatement(this.activeVersionCmd)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                    return null;
                }
            }
        }
    }
    
    /**
     * Delegate to the horcrux the data source closing
     */
    @Override
    public void close() {
        this.horcrux.close(this.ds);
    }
    
    /**
     * Open a new session
     * 
     * @return the new session
     * @throws SQLException
     */
    public Connection openSession() throws SQLException {
        return this.ds.getConnection();
    }

    /**
     * Returns the current version of the database.
     * 
     * @return the current version of the database or null if not found (error probably).
     * @throws SQLException
     */
    public Integer getCurrentVersion() throws SQLException {
        try (Connection conn = openSession()) {
            try (PreparedStatement statement = conn.prepareStatement(this.activeVersionCmd)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
                    }
                    return null;
                }
            }
        }
    }
    
    /**
     * Execute a SQL script file.
     * 
     * @param conn the open connection (not null)
     * @param file the path of the script file to execute (not null)
     * @throws IOException
     * @throws SQLException
     */
    public static void execute(final Connection conn, final Path file) throws IOException, SQLException {
        try(BufferedReader reader = Files.newBufferedReader(Objects.requireNonNull(file, "file is missing"), StandardCharsets.UTF_8)) {
            execute(conn, reader);
        }
    }
    
    /**
     * Execute a SQL script.
     * 
     * @param conn the open connection (not null)
     * @param reader the reader open on the script to execute (not null)
     * @throws SQLException
     * @throws IOException
     */
    public static void execute(final Connection conn, final BufferedReader reader) throws SQLException, IOException {
        String line = null;
        StringBuilder buff = new StringBuilder();
        try(Statement statement = Objects.requireNonNull(conn, "conn is missing").createStatement()) {
            while ((line = reader.readLine()) != null) {
                if(line.length() > 0 && line.charAt(0) == '-' || line.length() == 0 ) 
                    continue;
                buff.append(line);
                if (line.endsWith(";")) {
                    statement.execute(buff.toString());
                    buff = new StringBuilder();
                }
            }
        }
    }
    
}
