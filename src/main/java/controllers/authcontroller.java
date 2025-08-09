package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class authcontroller {
    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText();
            if (!username.isEmpty()) {
                app.MainApp.showMainScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignup() {
        try {
            String username = usernameField.getText();
            if (!username.isEmpty()) {
                app.MainApp.showMainScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}