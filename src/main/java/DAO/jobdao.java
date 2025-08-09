package DAO;

import utils.databaseconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class jobdao {
    private static final String CREATE_COMPANIES_TABLE = """
        CREATE TABLE IF NOT EXISTS companies (
            company_id INTEGER PRIMARY KEY AUTOINCREMENT,
            company_name TEXT NOT NULL UNIQUE
        )""";

    private static final String CREATE_JOBS_TABLE = """
        CREATE TABLE IF NOT EXISTS jobs (
            job_id INTEGER PRIMARY KEY AUTOINCREMENT,
            company_id INTEGER NOT NULL,
            job_title TEXT NOT NULL,
            job_description TEXT NOT NULL,
            average_percentage REAL NOT NULL,
            user_count INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY (company_id) REFERENCES companies(company_id)
        )""";

    public void initializeTables() throws SQLException {
        try (Connection conn = databaseconnection.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_COMPANIES_TABLE);
            stmt.execute(CREATE_JOBS_TABLE);
        }
    }

    // Add a new company
    public int addCompany(String companyName) throws SQLException {
        String sql = "INSERT INTO companies (company_name) VALUES (?)";
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, companyName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    // Add a new job position
    public void addJob(int companyId, String jobTitle, double averagePercentage) throws SQLException {
        String sql = """
            INSERT INTO jobs (company_id, job_title, average_percentage)
            VALUES (?, ?, ?)
            ON CONFLICT(company_id, job_title) 
            DO UPDATE SET 
                average_percentage = ((average_percentage * user_count) + ?) / (user_count + 1),
                user_count = user_count + 1""";

        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, companyId);
            pstmt.setString(2, jobTitle);
            pstmt.setDouble(3, averagePercentage);
            pstmt.setDouble(4, averagePercentage);
            pstmt.executeUpdate();
        }
    }

    // Get all jobs for a company
    public List<String> getCompanyJobs(int companyId) throws SQLException {
        List<String> jobs = new ArrayList<>();
        String sql = "SELECT job_title, average_percentage FROM jobs WHERE company_id = ?";

        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, companyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jobs.add(String.format("%s (%.1f%%)",
                        rs.getString("job_title"),
                        rs.getDouble("average_percentage")));
            }
        }
        return jobs;
    }
}