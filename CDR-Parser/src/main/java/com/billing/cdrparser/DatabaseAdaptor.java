package com.billing.cdrparser;

import com.telecom.postgrescore.Database;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.jooq.DSLContext;

/**
 * Provides database connection management and configurations for the application.
 * This class reads database credentials from {@code db.properties} and
 * initializes the database engine, returning a jOOQ query execution context.
 *
 * @author seifabsalam
 * @version 1.0
 * @since 1.0
 */
public class DatabaseAdaptor {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseAdaptor() {
        // Utility class has only static methods and should not be instantiated.
    }

    /**
     * Connects to the database and returns a DSL context.
     * Loads connection properties, validates credentials, and registers a JVM shutdown hook
     * to release resources gracefully.
     *
     * @return the configured jOOQ {@code DSLContext}
     * @throws IOException if {@code db.properties} cannot be opened or read
     * @throws IllegalStateException if any required database connection property is missing
     */
    public static DSLContext connect() throws IOException {
        Properties props = new Properties();
        try (var fis = new FileInputStream("db.properties")) {
            props.load(fis);
        }

        String url      = props.getProperty("db.url");
        String user     = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException(
                    "db.properties must define db.url, db.user, and db.password");
        }

        Database database = new Database(url, user, password);
        // Ensures that the connection pool is cleanly terminated when the application exits or is killed.
        Runtime.getRuntime().addShutdownHook(new Thread(database::close));

        return database.getDSLContext();
    }
}
