package com.trim.booking.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuration that wraps the default DataSource with RlsDataSource
 * when the 'rls' profile is active.
 *
 * This ensures that all database connections will have the tenant context set for Row-Level Security.
 */
@Configuration
@Profile("rls")
public class RlsDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        // Build a HikariDataSource from Spring Boot's auto-config properties
        HikariDataSource underlying = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        // autoCommit must be disabled so that SET LOCAL persists across
        // all queries within a transaction and Spring can commit/rollback.
        underlying.setAutoCommit(false);
        // Wrap it with RLS tenant context injection
        return new RlsDataSource(underlying);
    }
}

