package com.studysync.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Supports Render PostgreSQL DATABASE_URL values and legacy Aiven/MySQL DB_URL
 * values without requiring credentials to be committed to source control.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:#{null}}") String configuredUrl,
            @Value("${spring.datasource.username:#{null}}") String configuredUsername,
            @Value("${spring.datasource.password:#{null}}") String configuredPassword,
            @Value("${spring.datasource.driver-class-name:#{null}}") String configuredDriver) {

        return new HikariDataSource(
                resolveConfig(configuredUrl, configuredUsername, configuredPassword, configuredDriver)
        );
    }

    HikariConfig resolveConfig(
            String configuredUrl,
            String configuredUsername,
            String configuredPassword,
            String configuredDriver) {

        String databaseUrl = firstNonBlank(
                System.getenv("DATABASE_URL"),
                System.getProperty("DATABASE_URL"),
                System.getenv("DB_URL"),
                System.getProperty("DB_URL")
        );
        // Spring resolves the DB_URL property before injecting configuredUrl.
        // This fallback also allows DB_URL to contain a provider URI.
        if (!StringUtils.hasText(databaseUrl)) {
            databaseUrl = configuredUrl;
        }

        HikariConfig config = new HikariConfig();
        // Let the service finish booting during a brief database restart; Hikari
        // will continue trying to establish connections.
        config.setInitializationFailTimeout(-1);

        if (databaseUrl != null && databaseUrl.contains("${")) {
            log.warn("Database URL contains an unresolved placeholder; using configured datasource fallback");
            databaseUrl = null;
        }

        if (StringUtils.hasText(databaseUrl)) {
            if (databaseUrl.startsWith("jdbc:")) {
                config.setJdbcUrl(databaseUrl);
                configureDriver(config, databaseUrl);
                configureCredentials(config, configuredUsername, configuredPassword);
                return config;
            }

            boolean postgres = databaseUrl.startsWith("postgres://")
                    || databaseUrl.startsWith("postgresql://");
            boolean mysql = databaseUrl.startsWith("mysql://")
                    || databaseUrl.startsWith("mysqls://");

            if (postgres || mysql) {
                try {
                    String scheme = databaseUrl.substring(0, databaseUrl.indexOf("://"));
                    URI uri = URI.create("http://" + databaseUrl.substring(scheme.length() + 3));
                    String host = uri.getHost();
                    if (!StringUtils.hasText(host)) {
                        throw new IllegalArgumentException("Database URL has no hostname");
                    }

                    int port = uri.getPort() >= 0 ? uri.getPort() : (mysql ? 3306 : 5432);
                    String path = uri.getRawPath() == null ? "" : uri.getRawPath();
                    String jdbcUrl = (mysql ? "jdbc:mysql://" : "jdbc:postgresql://")
                            + host + ":" + port + path;
                    if (StringUtils.hasText(uri.getRawQuery())) {
                        jdbcUrl += "?" + uri.getRawQuery();
                    }

                    config.setJdbcUrl(jdbcUrl);
                    config.setDriverClassName(mysql
                            ? "com.mysql.cj.jdbc.Driver"
                            : "org.postgresql.Driver");

                    String userInfo = uri.getUserInfo();
                    if (StringUtils.hasText(userInfo)) {
                        String[] credentials = userInfo.split(":", 2);
                        config.setUsername(credentials[0]);
                        if (credentials.length > 1) {
                            config.setPassword(credentials[1]);
                        }
                    } else {
                        configureCredentials(config, configuredUsername, configuredPassword);
                    }
                    return config;
                } catch (RuntimeException ex) {
                    log.error("Unable to parse DATABASE_URL; falling back to configured datasource URL");
                }
            }
        }

        if (StringUtils.hasText(configuredUrl)) {
            config.setJdbcUrl(configuredUrl);
            configureDriver(config, configuredUrl);
        }
        configureCredentials(config, configuredUsername, configuredPassword);

        if (StringUtils.hasText(configuredDriver) && !StringUtils.hasText(config.getDriverClassName())) {
            config.setDriverClassName(configuredDriver);
        }
        return config;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private void configureDriver(HikariConfig config, String url) {
        if (url.startsWith("jdbc:mysql:")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:postgresql:")) {
            config.setDriverClassName("org.postgresql.Driver");
        } else if (url.startsWith("jdbc:h2:")) {
            config.setDriverClassName("org.h2.Driver");
        }
    }

    private void configureCredentials(HikariConfig config, String username, String password) {
        if (StringUtils.hasText(username)) {
            config.setUsername(username);
        }
        if (password != null) {
            config.setPassword(password);
        }
    }
}
