package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnection {
    private static Connection instance;

    public static synchronized Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection("jdbc:sqlite:./data/prototype.db");
        }
        return instance;
    }
}
