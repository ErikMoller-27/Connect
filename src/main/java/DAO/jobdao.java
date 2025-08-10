package DAO;

import models.job;
import utils.databaseconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class jobdao {

    public jobdao() {
        // no dbUrl needed here
    }

    public void initializeTables() throws SQLException {
        try (Connection conn = databaseconnection.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS companies (
                    companyId INTEGER PRIMARY KEY AUTOINCREMENT,
                    companyName TEXT NOT NULL UNIQUE
                )""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jobs (
                    jobId INTEGER PRIMARY KEY AUTOINCREMENT,
                    companyId INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    avgPercentage REAL NOT NULL,
                    userCount INTEGER DEFAULT 1,
                    FOREIGN KEY (companyId) REFERENCES companies(companyId),
                    UNIQUE(companyId, title)
                )""");
        }
    }

    public int addCompany(String companyName) throws SQLException {
        String sql = "INSERT INTO companies (companyName) VALUES (?)";
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, companyName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public void addJob(int companyId, String title, String description, double avgPercentage) throws SQLException {
        String sql = """
            INSERT INTO jobs (companyId, title, description, avgPercentage)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(companyId, title) 
            DO UPDATE SET 
                description = excluded.description,
                avgPercentage = ((avgPercentage * userCount) + excluded.avgPercentage) / (userCount + 1),
                userCount = userCount + 1""";

        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, companyId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setDouble(4, avgPercentage);
            pstmt.executeUpdate();
        }
    }

    public List<job.JobListing> getCompanyJobs(int companyId) throws SQLException {
        List<job.JobListing> jobs = new ArrayList<>();
        String sql = """
            SELECT title, description, avgPercentage, userCount 
            FROM jobs 
            WHERE companyId = ?""";

        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, companyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jobs.add(new job.JobListing(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDouble("avgPercentage")
                ));
            }
        }
        return jobs;
    }

    public job getCompanyWithJobs(int companyId) throws SQLException {
        String nameSql = "SELECT companyName FROM companies WHERE companyId = ?";
        String companyName;
        try (Connection conn = databaseconnection.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(nameSql)) {
            pstmt.setInt(1, companyId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) throw new SQLException("Company not found");
            companyName = rs.getString("companyName");
        }

        job company = new job(companyId, companyName);
        for (job.JobListing job : getCompanyJobs(companyId)) {
            company.addJob(
                    job.getTitle(),
                    job.getDescription(),
                    job.getAvgPercentage()
            );
        }
        return company;
    }
}
