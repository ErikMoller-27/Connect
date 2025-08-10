package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnection {
    private static Connection instance;

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            // Ensure parent folder exists
            try {
                Path dbPath = Path.of("./data");
                if (!Files.exists(dbPath)) {
                    Files.createDirectories(dbPath);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create DB folder: " + e.getMessage(), e);
            }

            // SQLite (creates file if missing)
            String url = "jdbc:sqlite:./data/prototype.db";
            instance = DriverManager.getConnection(url);
        }
        return instance;
    }
}
