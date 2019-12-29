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
package com.code.fauch.horcrux.spi;

import java.util.Properties;

import javax.sql.DataSource;

/**
 * Describes the expected behavior of a horcrux.
 * Horcrux know how to build and close a data source.
 * 
 * @author c.fauch
 *
 */
public interface IHorcrux {

    /**
     * Builds and returns a new Data Source.
     * 
     * @param properties properties of the data source to build (not null)
     * @return the new data source
     */
    DataSource newDataSource(Properties properties);

    /**
     * Close all connections of a DataSource
     * The data source instance if the one return by the <code>newDataSource</code> method
     * 
     * @param ds the data source to close (not null).
     */
    void close(DataSource ds);

    /**
     * Indicates if this horcrux can be used the type requested;
     * 
     * @param type the type of horcrux (not null)
     * @return true if this horcrux can be associated with the given type.
     */
    boolean accept(String type);
    
}
