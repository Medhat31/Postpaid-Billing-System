package com.billing.billreporter;


import com.telecom.postgrescore.Database;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.jooq.DSLContext;

public class DatabaseAdaptor {

    private DatabaseAdaptor() {
        // Utility class has only static methods and should not be instantiated.
    }
    
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
