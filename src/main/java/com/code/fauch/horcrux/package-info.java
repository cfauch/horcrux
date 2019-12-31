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
/**
 * <p>
 * The main API for database access and management.
 * </p>
 * <h3>Setting up</h3>
 * <p>
 * Creates a directory with following files:
 * <ul>
 * <li><code>schema.sql</code>: containing data definitions</li>
 * <li><code>populate.sql</code>: containing all known database versions</li>
 * <li>all migration scripts to apply to upgrade the database
 * </ul>
 * </p>
 * <h3>Database access and management</h3>
 * <p>
 * First, instantiate {@linkplain com.code.fauch.horcrux.DataBase}, like this:
 * </p>
 * <pre>
 *         final DataBase ddb = DataBase.init("pool")
                .withScripts(Paths.get(getClass().getResource("/dataset/v1").toURI()))
                .versionTable("HORCRUX_VERSIONS")
                .build(prop);
 * </pre>
 * <p>
 * <ul>
 * <li><code>DataBase.init("pool")</code> initializes the <code>DataBase</code> object with a 
 * connection pool available in the application class path. You can also use <code>DataBase.init("basic")</code>
 * if you want to use a simple data source without connection pool.</li>
 * <li><code>withScripts(...)</code> specifies the path of the directory containing migration scripts, 
 * <code>schema.sql</code> and <code>populate.sql</code>. 
 * By default the directory used is the root folder of the application.</li>
 * <li><code>versionTable("HORCRUX_VERSIONS")</code> specifies the name of the table containing all database versions
 * and their corresponding migration script.</li>
 * <li><code>build(...)</code>creates the <code>DataBase</code> instance and initialize the data source with the
 * given properties</li>
 * </ul>
 * </p>
 * <p>
 * Then, apply <code>DataBase.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE)</code>. This instruction first checks
 * whether the database is empty or not. if the database is empty and <code>ECreation.SCHEMA</code> is present, 
 * then the <code>schema.sql</code> script is executed before to run the <code>populate.sql</code> script. Next,
 * if the database needs to be upgraded and <code>ECreation.UPGRADE</code> is present, migration scripts are applied.
 * </p>
 * <p>
 * If the database is empty but <code>ECreateOption.SCHEMA</code> is not present, a <code>SQLWarning</code> 
 * is raised. If the database needs to be upgraded but <code>ECreateOption.UPGRADE</code> is not present, a 
 * <code>SQLWarning</code> is raised.
 * </p>
 * <p>
 * Now, you can call <code>DataBase.openSession()</code> to obtain a connection to the database.
 * </p>
 * <h3>Example with HikariCP data source and postgresql</h3>
 * <pre>
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", "jdbc:postgresql:hx");
        prop.setProperty("username", "totoro");
        prop.setProperty("password", "2what4?");
        prop.setProperty("autoCommit", "false");
        prop.setProperty("poolName", "database-connection-pool");
        final Path scripts = Paths.get(Main.class.getResource("/db").toURI());
        try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
            Assert.assertEquals(2, db.getCurrentVersion().intValue());
        }
 * </pre>
 * <h3>Example with simple data source and in memory H2</h3>
 * <pre>
 *      final Properties prop = new Properties();
 *      prop.setProperty("jdbcUrl", "jdbc:h2:mem:v1;DB_CLOSE_DELAY=-1");
 *      prop.setProperty("username", "harry");
 *      prop.setProperty("password", "");
 *      final Path scripts = Paths.get(getClass().getResource("/dataset/v2").toURI());
 *      try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
 *          db.open(ECreateOption.SCHEMA);
 *          Assert.assertEquals(2, db.getCurrentVersion().intValue());
 *      }
 * </pre>
 * 
 * @author c.fauch
 *
 */
package com.code.fauch.horcrux;

