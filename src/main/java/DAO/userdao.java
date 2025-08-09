package DAO;

import utils.databaseconnection;

import java.sql.*;
import java.util.*;

public class userdao {
    // Initialize database and tables
    public void initializeDatabase() throws SQLException {
        try (Connection conn = databaseconnection.getInstance();
             Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    userId INTEGER PRIMARY KEY AUTOINCREMENT,
                    firstName TEXT NOT NULL
                )""");
            // Create skills table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS skills (
                    skillId INTEGER PRIMARY KEY AUTOINCREMENT,
                    userId INTEGER NOT NULL,
                    subject TEXT NOT NULL,
                    percentage INTEGER NOT NULL,
                    FOREIGN KEY (userId) REFERENCES users(userId)
                )""");
        }
    }

    // Create user and return generated ID
    public int createUser(String firstName) throws SQLException {
        String sql = "INSERT INTO users (firstName) VALUES (?)";
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1; // Return userId
        }
    }

    // Add skills for a user (from resume analysis)
    public void addSkills(int userId, Map<String, Integer> skills) throws SQLException {
        String sql = "INSERT INTO skills (userId, subject, percentage) VALUES (?, ?, ?)";
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : skills.entrySet()) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.addBatch(); // Batch insert for efficiency
            }
            pstmt.executeBatch();
        }
    }

    // Get skills for a user (to display graph)
    public Map<String, Integer> getSkills(int userId) throws SQLException {
        String sql = "SELECT subject, percentage FROM skills WHERE userId = ?";
        Map<String, Integer> skills = new HashMap<>();
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                skills.put(rs.getString("subject"), rs.getInt("percentage"));
            }
        }
        return skills;
    }
}