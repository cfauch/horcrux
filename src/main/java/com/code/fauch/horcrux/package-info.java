/*
 * Copyright 2021 Claire Fauch
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
 * Database versioning.
 * </p>
 * <h3>Setting up</h3>
 * <p>
 * Creates a directory with following files:
 * <ul>
 * <li><code>schema.sql</code>: with database definitions and eventually some predefined records</li>
 * <li><code>populate.sql</code>: with all known versions</li>
 * <li>all scripts to apply to update the database
 * </ul>
 * </p>
 * <h3>Updating the database</h3>
 * <pre>
 *        final Migration migration = new Migration.Builder()
 *             .versionTable("HORCRUX_VERSIONS)
 *             .createSchema(true)
 *             .runUpdates(true)
 *             .withScripts(Paths.get(getClass().getResource("/dataset/v3").toURI()))
 *             .build();
 *        migration.update(ds);
 * </pre>
 * <p>
 * Use the builder <code>Migration.Builder</code> to build a new <code>Migration</code> object.
 * <ul>
 * <li><code>withScripts(...)</code> specifies the path of the directory containing migration scripts,
 * <code>schema.sql</code> and <code>populate.sql</code>. 
 * By default the directory used is the root folder of the application.</li>
 * <li><code>versionTable("HORCRUX_VERSIONS")</code> specifies the name of the table containing all database
 * versions and their corresponding migration script.</li>
 * <li><code>createSchema(true)</code> specifies to apply the script <code>schema.sql</code> if the database is
 * empty.</li>
 * <li><code>runUpdates(true)</code> specifies to apply the migration scripts if the database is too old.</li>
 * <li><code>build()</code>creates the <code>Migration</code>.</li>
 * </ul>
 * Then, apply <code>Migration.update(DataSource)</code> to update the given database.
 * </p>
 * <h3>Example with in memory H2</h3>
 * <pre>
 *         final DataSource ds = JdbcConnectionPool.create(
 *                 "jdbc:h2:mem:v1;DB_CLOSE_DELAY=1",
 *                 "harry",
 *                 ""
 *         );
 *         final Migration migration = new Migration.Builder()
 *                 .versionTable("horcrux_versions".toUpperCase())
 *                 .createSchema(true)
 *                 .runUpdates(true)
 *                 .withScripts(Paths.get(getClass().getResource("/dataset/v1").toURI()))
 *                 .build();
 *         migration.update(ds);
 * </pre>
 * 
 * @author c.fauch
 *
 */
package com.code.fauch.horcrux;

