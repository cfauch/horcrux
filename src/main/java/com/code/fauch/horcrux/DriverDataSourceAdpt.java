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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Adapter to adapt <code>DriverManager</code> to be use as <code>DataSource</code>
 * It is a simple <code>DataSource</code> without pooling connection.
 * 
 * @author c.fauch
 *
 */
final class DriverDataSourceAdpt implements DataSource {

    /**
     * The database URL.
     */
    private final String jdbcUrl;
    
    /**
     * The database user name.
     */
    private final String user;
    
    /**
     * The database user password.
     */
    private final String password;
    
    /**
     * Constructor
     * 
     * @param jdbcUrl the database URL
     */
    DriverDataSourceAdpt(final String jdbcUrl, final String user, final String pwd) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = pwd;
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.jdbcUrl, this.user, this.password);
    }

    @Override
    public Connection getConnection(final String user, final String password) throws SQLException {
        return DriverManager.getConnection(this.jdbcUrl, user, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

}
