package io.lana.sqlstarter.repo.conn;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtils {
    private ConnectionUtils() {
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
