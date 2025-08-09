import DAO.jobdao;
import DAO.userdao;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To use @BeforeAll non-static if needed
class DatabaseTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test.db";
    private Connection testConn;
    private userdao userDao;
    private jobdao jobDao;

    @BeforeAll
    void setupDatabase() throws SQLException {
        testConn = DriverManager.getConnection(TEST_DB_URL);
        try (Statement stmt = testConn.createStatement()) {
            // Drop tables if exist
            stmt.execute("DROP TABLE IF EXISTS skills");
            stmt.execute("DROP TABLE IF EXISTS jobs");
            stmt.execute("DROP TABLE IF EXISTS companies");
            stmt.execute("DROP TABLE IF EXISTS users");
        }
    }

    @BeforeEach
    void initDaos() throws SQLException {
        userDao = new userdao(TEST_DB_URL);
        jobDao = new jobdao(TEST_DB_URL);
        userDao.initializeTables();
        jobDao.initializeTables();
    }

    @Test
    void testUserCreation() throws SQLException {
        int userId = userDao.createUser("TestUser");
        assertTrue(userId > 0, "User ID should be positive");
    }

    @Test
    void testSkillInsertion() throws SQLException {
        int userId = userDao.createUser("SkillTest");
        Map<String, Integer> skills = Map.of("Java", 85, "Teamwork", 70);
        userDao.addSkills(userId, skills);

        Map<String, Integer> retrieved = userDao.getSkills(userId);
        assertEquals(2, retrieved.size());
        assertEquals(85, retrieved.get("Java"));
        assertEquals(70, retrieved.get("Teamwork"));
    }

    @Test
    void testJobCreation() throws SQLException {
        int companyId = jobDao.addCompany("TestCorp");
        jobDao.addJob(companyId, "Developer", "Backend work", 80.5);

        var jobs = jobDao.getCompanyJobs(companyId);
        assertEquals(1, jobs.size());
        assertEquals("Developer", jobs.get(0).getTitle());
        assertEquals(80.5, jobs.get(0).getAvgPercentage(), 0.01);
    }

    @Test
    void testJobAverageUpdate() throws SQLException {
        int companyId = jobDao.addCompany("AvgTest");
        jobDao.addJob(companyId, "Designer", "UI work", 75.0);
        jobDao.addJob(companyId, "Designer", "UI work", 85.0);

        var jobs = jobDao.getCompanyJobs(companyId);
        assertEquals(1, jobs.size());
        assertEquals("Designer", jobs.get(0).getTitle());
        assertEquals(80.0, jobs.get(0).getAvgPercentage(), 0.01);
    }

    @AfterAll
    void cleanup() throws SQLException {
        if (testConn != null && !testConn.isClosed()) {
            testConn.close();
        }
    }
}
