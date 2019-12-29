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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author c.fauch
 *
 */
public class DataBaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testEmptyBaseFirstVersion() throws URISyntaxException, SQLException, IOException {
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", "jdbc:h2:mem:test_mem");
        prop.setProperty("username", "harry");
        prop.setProperty("password", "");
        final Path scripts = Paths.get(getClass().getResource("/dataset/v1").toURI());
        try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            final Integer version = db.open(ECreateOption.SCHEMA);
            Assert.assertEquals(1, version.intValue());
        }
    }

    @Test(expected=SQLWarning.class)
    public void testEmptyBaseWithoutSchema() throws URISyntaxException, SQLException, IOException {
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", "jdbc:h2:mem:test_mem");
        prop.setProperty("username", "harry");
        prop.setProperty("password", "");
        final Path scripts = Paths.get(getClass().getResource("/dataset/v1").toURI());
        try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            final Integer version = db.open();
            Assert.assertEquals(1, version.intValue());
        }
     }
    
    @Test
    public void testEmptyBaseScdVersion() throws URISyntaxException, SQLException, IOException {
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", "jdbc:h2:mem:test_mem");
        prop.setProperty("username", "harry");
        prop.setProperty("password", "");
        final Path scripts = Paths.get(getClass().getResource("/dataset/v2").toURI());
        try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            final Integer version = db.open(ECreateOption.SCHEMA);
            Assert.assertEquals(2, version.intValue());
        }
    }
    
    @Test(expected=SQLWarning.class)
    public void testOldExistingBaseScdVersionWithoutUpgrade() throws URISyntaxException, SQLException, IOException {
        final URI dbFile = getClass().getResource("/dataset/v1/").toURI().resolve("test_mem");
        final String url = String.format("jdbc:h2:%s", dbFile.getPath());
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", url);
        prop.setProperty("username", "harry");
        prop.setProperty("password", "");
        final Path scripts = Paths.get(getClass().getResource("/dataset/v2").toURI());
        try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            final Integer version = db.open(ECreateOption.SCHEMA);
            Assert.assertEquals(1, version.intValue());
        }
    }
    
    @Test
    public void testOldExistingBaseScdVersionUpgrade() throws URISyntaxException, SQLException, IOException {
        final Path dbFile = this.folder.getRoot().toPath().resolve("yo.mv.db");
        Files.copy(Paths.get(getClass().getResource("/dataset/v1/test_mem.mv.db").toURI()), dbFile);
        final String url = String.format("jdbc:h2:%s", this.folder.getRoot().toURI().resolve("yo"));
        final Properties prop = new Properties();
        prop.setProperty("jdbcUrl", url);
        prop.setProperty("username", "harry");
        prop.setProperty("password", "");
        prop.setProperty("autoCommit", "false");
        prop.setProperty("poolName", "database-connection-pool");
        final Path scripts = Paths.get(getClass().getResource("/dataset/v2").toURI());
        try(DataBase db = DataBase.init("basic").withScripts(scripts).versionTable("horcrux_versions").build(prop)) {
            final Integer version = db.open(ECreateOption.UPGRADE);
            Assert.assertEquals(2, version.intValue());
        }
    }
    
}
