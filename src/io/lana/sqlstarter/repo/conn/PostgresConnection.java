package io.lana.sqlstarter.repo.conn;

import java.sql.Connection;

public enum PostgresConnection {
    INSTANCE;

    private final Connection connection;

    PostgresConnection() {
        this.connection = ConnectionUtils.getConnection("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/bkap?user=postgres&password=123456");
    }

    public Connection getConnection() {
        return connection;
    }
}
