import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app extends Application {
    private static Stage primaryStage; // Store the primary stage

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showLoginScreen(); // Start with login screen
    }

    public void showLoginScreen() {
        try {
            // Load login.fxml from resources folder
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Call this method after successful login
    public static void showMainScreen() {
        try {
            Parent root = FXMLLoader.load(app.class.getResource("/profile.fxml"));
            primaryStage.setTitle("Main App");
            primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Fix JavaFX warning by calling launch() properly
        launch(args);
    }
}