import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class authcontroller {
    @FXML private TextField nameField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    @FXML
    private void handleAuth() throws Exception {
        String fullName = nameField.getText().trim();
        if (!fullName.isEmpty()) {
            // Close auth window
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();

            // Open main application
            Parent root = FXMLLoader.load(getClass().getResource("/resources/main.fxml"));
            Stage stage = new Stage();
            stage.setTitle("RadialMatch - Welcome " + fullName);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        }
    }
}