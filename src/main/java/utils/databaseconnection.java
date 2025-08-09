package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnection {
    private static Connection instance;

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            // SQLite (creates file if missing)
            String url = "jdbc:sqlite:./data/prototype.db";
            instance = DriverManager.getConnection(url);
        }
        return instance;
    }
}