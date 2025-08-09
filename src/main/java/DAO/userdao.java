package DAO;

import models.user;
import java.sql.*;
import java.util.*;

public class userdao {
    private final String dbUrl;

    public userdao(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public userdao() {
        this("jdbc:sqlite:main.db");
    }

    public void initializeTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    userId INTEGER PRIMARY KEY AUTOINCREMENT,
                    firstName TEXT NOT NULL
                )""");
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

    public int createUser(String firstName) throws SQLException {
        String sql = "INSERT INTO users (firstName) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public void addSkills(int userId, Map<String, Integer> skills) throws SQLException {
        String sql = "INSERT INTO skills (userId, subject, percentage) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Integer> entry : skills.entrySet()) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Replace all skills for a user in a single transaction (prevents duplicates).
     * Deletes existing rows for userId, then inserts the provided (subject, percentage) pairs.
     */
    public void replaceSkills(int userId, Map<String, Integer> skills) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM skills WHERE userId = ?")) {
                del.setInt(1, userId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO skills (userId, subject, percentage) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, Integer> e : skills.entrySet()) {
                    ins.setInt(1, userId);
                    ins.setString(2, e.getKey());
                    ins.setInt(3, e.getValue());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            conn.commit();
        }
    }

    public Map<String, Integer> getSkills(int userId) throws SQLException {
        Map<String, Integer> skills = new HashMap<>();
        String sql = "SELECT subject, percentage FROM skills WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                skills.put(rs.getString("subject"), rs.getInt("percentage"));
            }
        }
        return skills;
    }

    public user getUserProfile(int userId) throws SQLException {
        // Get user name
        String nameSql = "SELECT firstName FROM users WHERE userId = ?";
        String firstName;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(nameSql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("User not found with ID: " + userId);
            }
            firstName = rs.getString("firstName");
        }

        // Create and populate user profile
        user profile = new user(userId, firstName);
        profile.addSkills(getSkills(userId));
        return profile;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public int getUserIdByName(String firstName) throws SQLException {
        String sql = "SELECT userId FROM users WHERE firstName = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("userId");
            } else {
                return -1; // Not found
            }
        }
    }
}
