package app;

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
        private static Stage primaryStage;
        private static int loggedInUserId = -1;  // Store logged in user ID globally

        @Override
        public void start(Stage stage) throws Exception {
            primaryStage = stage;
            showAuthScreen();
        }

        public static void showAuthScreen() throws Exception {
            Parent root = FXMLLoader.load(MainApp.class.getResource("/auth.fxml"));
            primaryStage.setTitle("RadialMatch - Login");
            primaryStage.setScene(new Scene(root, 400, 400));
            primaryStage.show();
        }

        public static void showMainScreen(int userId) throws Exception {
            loggedInUserId = userId;
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/main.fxml"));
            Parent root = loader.load();

            // Get controller and pass logged-in user ID
            controllers.maincontroller mainController = loader.getController();
            mainController.loadUserProfile(userId);

            primaryStage.setTitle("RadialMatch - Main");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        }

        public static int getLoggedInUserId() {
            return loggedInUserId;
        }
    }
}
