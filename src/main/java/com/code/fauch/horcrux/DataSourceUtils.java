package com.code.fauch.horcrux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

final class DataSourceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);

    /**
     * Execute a SQL script file.
     *
     * @param conn the open connection (not null)
     * @param file the path of the script file to execute (not null)
     * @throws IOException
     * @throws SQLException
     */
    static void execute(final Connection conn, final Path file) throws IOException, SQLException {
        LOGGER.info("running script: {}", file);
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
    static void execute(final Connection conn, final BufferedReader reader) throws SQLException, IOException {
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
