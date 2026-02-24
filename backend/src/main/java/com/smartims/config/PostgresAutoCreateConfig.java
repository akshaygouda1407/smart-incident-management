package com.smartims.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class PostgresAutoCreateConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${app.db.auto-create.enabled:true}")
    private boolean autoCreateDatabaseEnabled;

    @Bean
    @Primary
    public DataSource dataSource() {
        if (autoCreateDatabaseEnabled) {
            ensurePostgresDatabaseExists();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(datasourceUrl);
        config.setUsername(datasourceUsername);
        config.setPassword(datasourcePassword);
        config.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(config);
    }

    private void ensurePostgresDatabaseExists() {
        ParsedJdbcUrl parsed = parsePostgresUrl(datasourceUrl);
        if (parsed == null || parsed.databaseName == null || parsed.databaseName.isBlank()) {
            return;
        }

        String adminUrl = parsed.baseUrl + "/postgres" + (parsed.query == null || parsed.query.isBlank() ? "" : "?" + parsed.query);

        try (Connection connection = DriverManager.getConnection(adminUrl, datasourceUsername, datasourcePassword)) {
            if (databaseExists(connection, parsed.databaseName)) {
                return;
            }

            String escapedDbName = parsed.databaseName.replace("\"", "\"\"");
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + escapedDbName + "\"");
            }
        } catch (Exception ex) {
            throw new IllegalStateException(
                "Failed to auto-create PostgreSQL database '" + parsed.databaseName + "'. " +
                    "Ensure user has CREATEDB privilege or set APP_DB_AUTO_CREATE_ENABLED=false.",
                ex
            );
        }
    }

    private boolean databaseExists(Connection connection, String databaseName) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT 1 FROM pg_database WHERE datname = ?")) {
            ps.setString(1, databaseName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private ParsedJdbcUrl parsePostgresUrl(String url) {
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return null;
        }

        String raw = url.substring("jdbc:postgresql://".length());
        String hostAndPath;
        String query = null;
        int queryIndex = raw.indexOf('?');
        if (queryIndex >= 0) {
            hostAndPath = raw.substring(0, queryIndex);
            query = raw.substring(queryIndex + 1);
        } else {
            hostAndPath = raw;
        }

        int slashIndex = hostAndPath.indexOf('/');
        if (slashIndex < 0 || slashIndex == hostAndPath.length() - 1) {
            return null;
        }

        String hostPort = hostAndPath.substring(0, slashIndex);
        String database = hostAndPath.substring(slashIndex + 1);
        String baseUrl = "jdbc:postgresql://" + hostPort;
        return new ParsedJdbcUrl(baseUrl, database, query);
    }

    private static final class ParsedJdbcUrl {
        private final String baseUrl;
        private final String databaseName;
        private final String query;

        private ParsedJdbcUrl(String baseUrl, String databaseName, String query) {
            this.baseUrl = baseUrl;
            this.databaseName = databaseName;
            this.query = query;
        }
    }
}
