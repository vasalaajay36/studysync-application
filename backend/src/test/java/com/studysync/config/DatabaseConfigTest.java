package com.studysync.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    private final DatabaseConfig databaseConfig = new DatabaseConfig();

    @AfterEach
    void clearDatabaseUrl() {
        System.clearProperty("DATABASE_URL");
    }

    @Test
    void usesConfiguredH2PropertiesForTests() {
        HikariConfig config = databaseConfig.resolveConfig(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver");

        assertEquals("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", config.getJdbcUrl());
        assertEquals("sa", config.getUsername());
        assertEquals("org.h2.Driver", config.getDriverClassName());
    }

    @Test
    void parsesRenderPostgresConnectionString() {
        System.setProperty("DATABASE_URL",
                "postgresql://renderuser:secretpass@db.internal:5432/studysync_db?sslmode=require");

        HikariConfig config = databaseConfig.resolveConfig(null, null, null, null);

        assertEquals(
                "jdbc:postgresql://db.internal:5432/studysync_db?sslmode=require",
                config.getJdbcUrl());
        assertEquals("renderuser", config.getUsername());
        assertEquals("secretpass", config.getPassword());
        assertEquals("org.postgresql.Driver", config.getDriverClassName());
    }

    @Test
    void parsesAivenMysqlConnectionString() {
        System.setProperty("DATABASE_URL",
                "mysql://avnadmin:mypassword@mysql.example.com:20055/defaultdb?ssl-mode=REQUIRED");

        HikariConfig config = databaseConfig.resolveConfig(null, null, null, null);

        assertEquals(
                "jdbc:mysql://mysql.example.com:20055/defaultdb?ssl-mode=REQUIRED",
                config.getJdbcUrl());
        assertEquals("avnadmin", config.getUsername());
        assertEquals("mypassword", config.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriverClassName());
    }

    @Test
    void ignoresUnresolvedDatabaseUrlPlaceholder() {
        System.setProperty("DATABASE_URL",
                "postgres://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}");

        HikariConfig config = databaseConfig.resolveConfig(
                "jdbc:mysql://localhost:3306/fallbackdb", "root", "pass", null);

        assertEquals("jdbc:mysql://localhost:3306/fallbackdb", config.getJdbcUrl());
        assertEquals("root", config.getUsername());
        assertEquals("pass", config.getPassword());
        assertEquals("com.mysql.cj.jdbc.Driver", config.getDriverClassName());
    }
}
