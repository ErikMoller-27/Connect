import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }

    public static class MainApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            // Load CSS separately to avoid FXML issues
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            Parent root = loader.load();

            // Apply CSS programmatically
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setTitle("Career Analyzer");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}