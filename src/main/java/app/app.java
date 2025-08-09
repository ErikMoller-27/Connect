package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app extends Application {

    private static Stage primaryStage; // Store stage so we can change scenes later

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showLoginScreen(); // Show login on startup
    }

    public static void main(String[] args) {
        launch();
    }

    public static void showLoginScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(app.class.getResource("/login.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void showMainScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(app.class.getResource("/main.fxml")); // change to your main screen fxml
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Main Screen");
        primaryStage.show();
    }
}
