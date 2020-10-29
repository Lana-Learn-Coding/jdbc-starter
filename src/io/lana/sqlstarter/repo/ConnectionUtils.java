package io.lana.sqlstarter.repo;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtils {
    private ConnectionUtils() {
    }

    public static Connection getConnection() {
        return getConnection("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/bkap?user=postgres&password=123456");
    }

    public static Connection getConnection(String className, String connectionString) {
        try {
            Class.forName(className);
            return DriverManager.getConnection(connectionString);
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
}
