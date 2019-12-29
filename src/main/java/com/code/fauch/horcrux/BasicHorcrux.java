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

import java.util.Properties;

import javax.sql.DataSource;

import com.code.fauch.horcrux.spi.IHorcrux;

/**
 * Basic horcrux implementations that manages a simple data source without pooling connection.
 * 
 * @author c.fauch
 *
 */
public final class BasicHorcrux implements IHorcrux {
    
    /**
     * The expected type name.
     */
    private static final String TYPE = "basic";

    @Override
    public DataSource newDataSource(Properties properties) {
        return new DriverDataSourceAdpt(
                properties.getProperty("jdbcUrl"), 
                properties.getProperty("username"),
                properties.getProperty("password")
        );
    }

    @Override
    public void close(DataSource ds) {
        // Nothing to do: connection are not kept
    }

    @Override
    public boolean accept(String type) {
        return TYPE.equals(type);
    }

}
