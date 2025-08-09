import DAO.userdao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/path/to/your.fxml"));
        primaryStage.setTitle("Your Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Optional: Test database connection on startup
        testDatabaseConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void testDatabaseConnection() {
        try {
            userdao userDao = new userdao();
            userDao.initializeTables();
            System.out.println("Database connection successful!");

            // Simple CRUD test
            int userId = userDao.createUser("Test User");
            System.out.println("Created user with ID: " + userId);
        } catch (Exception e) {
            System.err.println("Database connection failed:");
            e.printStackTrace();
        }
    }
}