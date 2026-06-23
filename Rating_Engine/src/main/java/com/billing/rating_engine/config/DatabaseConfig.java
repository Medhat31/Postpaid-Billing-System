package com.billing.rating_engine.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static HikariDataSource dataSource;
    private static DSLContext dslContext;

    public static synchronized void initialize() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/billing");
            config.setUsername("postgres");
            config.setPassword("1234");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            
            dataSource = new HikariDataSource(config);
            dslContext = DSL.using(dataSource, SQLDialect.POSTGRES);
        }
    }

    public static DSLContext getDSLContext() {
        if (dslContext == null) {
            initialize();
        }
        return dslContext;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}