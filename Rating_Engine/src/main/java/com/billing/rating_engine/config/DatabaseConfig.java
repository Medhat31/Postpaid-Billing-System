package com.billing.rating_engine.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {

    private static HikariDataSource dataSource;
    private static DSLContext dslContext;

    public static synchronized void initialize() throws FileNotFoundException, IOException {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();

            Properties props = new Properties();
            try (var fis = new FileInputStream("db.properties")) {
                props.load(fis);
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            dataSource = new HikariDataSource(config);
            dslContext = DSL.using(dataSource, SQLDialect.POSTGRES);
        }
    }

    public static DSLContext getDSLContext() throws FileNotFoundException, IOException {
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
