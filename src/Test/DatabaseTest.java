import DAO.jobdao;
import DAO.userdao;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test.db";
    private static Connection testConn;
    private userdao userDao;
    private jobdao jobDao;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        // Create fresh test database
        testConn = DriverManager.getConnection(TEST_DB_URL);
        try (Statement stmt = testConn.createStatement()) {
            // Drop existing tables
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("DROP TABLE IF EXISTS skills");
            stmt.execute("DROP TABLE IF EXISTS companies");
            stmt.execute("DROP TABLE IF EXISTS jobs");
        }
    }

    @BeforeEach
    void initDaos() throws SQLException {
        userDao = new userdao(TEST_DB_URL); // Modified constructor
        jobDao = new jobdao(TEST_DB_URL);   // Modified constructor
        userDao.initializeTables();
        jobDao.initializeTables();
    }

    @Test
    void testUserCreation() throws SQLException {
        // Test
        int userId = userDao.createUser("TestUser");

        // Verify
        assertTrue(userId > 0, "Should return valid user ID");
    }

    @Test
    void testSkillInsertion() throws SQLException {
        // Setup
        int userId = userDao.createUser("SkillTest");
        Map<String, Integer> skills = Map.of("Java", 85, "Teamwork", 70);

        // Test
        userDao.addSkills(userId, skills);

        // Verify
        Map<String, Integer> retrieved = userDao.getSkills(userId);
        assertEquals(2, retrieved.size(), "Should store 2 skills");
        assertEquals(85, retrieved.get("Java"), "Java skill should be 85%");
    }

    @Test
    void testJobCreation() throws SQLException {
        // Setup
        int companyId = jobDao.addCompany("TestCorp");

        // Test
        jobDao.addJob(companyId, "Developer", "Backend work", 80.5);

        // Verify
        var jobs = jobDao.getCompanyJobs(companyId);
        assertEquals(1, jobs.size(), "Company should have 1 job");
        assertEquals("Developer", jobs.get(0).getTitle());
    }

    @Test
    void testJobAverageUpdate() throws SQLException {
        // Setup
        int companyId = jobDao.addCompany("AvgTest");
        jobDao.addJob(companyId, "Designer", "UI work", 75.0);

        // Test - Add same job with new percentage
        jobDao.addJob(companyId, "Designer", "UI work", 85.0);

        // Verify
        var jobs = jobDao.getCompanyJobs(companyId);
        assertEquals(1, jobs.size(), "Should update existing job");
        assertEquals(80.0, jobs.get(0).getAvgPercentage(), 0.01, "Average should be 80%");
    }

    @AfterAll
    static void cleanup() throws SQLException {
        if (testConn != null) {
            testConn.close();
        }
    }
}