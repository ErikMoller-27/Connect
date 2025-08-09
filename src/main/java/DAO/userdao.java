package DAO;

import models.user;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class userdao {
    private final String dbUrl = "jdbc:sqlite:main.db";

    public userdao() {
        try {
            initializeTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    FOREIGN KEY (userId) REFERENCES users(userId),
                    UNIQUE(userId, subject)
                )""");

            // Add a test user if none exist
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (firstName) VALUES ('erik')");
            }
        }
    }

    public int createUser(String firstName) throws SQLException {
        String sql = "INSERT INTO users (firstName) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // return new userId
                }
            }
        }
        throw new SQLException("Creating user failed, no ID obtained.");
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
                return -1;
            }
        }
    }

    public user getUserProfile(int userId) throws SQLException {
        String sql = "SELECT firstName FROM users WHERE userId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) throw new SQLException("User not found");

            String firstName = rs.getString("firstName");
            user u = new user(userId, firstName);
            u.addSkills(getSkills(userId));
            return u;
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

    public void addSkills(int userId, Map<String, Integer> skills) throws SQLException {
        String sql = """
            INSERT INTO skills (userId, subject, percentage)
            VALUES (?, ?, ?)
            ON CONFLICT(userId, subject) DO UPDATE SET percentage=excluded.percentage
            """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (var entry : skills.entrySet()) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, entry.getValue());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
