package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import utils.session;
import app.app;  // Your main app class for screen switching

public class authcontroller {
    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private final userdao userDao = new userdao();

    @FXML
    public void initialize() {
        try {
            userDao.initializeTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText();
            if (!username.isEmpty()) {
                int userId = userDao.getUserIdByName(username);
                if (userId != -1) {
                    System.out.println("Logged in as existing user: " + username + " (ID: " + userId + ")");
                    session.setCurrentUserId(userId);   // Save logged in user globally
                    app.showMainScreen();                // Switch screen
                } else {
                    System.out.println("User not found, please sign up first.");
                }
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
                int existingUserId = userDao.getUserIdByName(username);
                if (existingUserId == -1) {
                    int userId = userDao.createUser(username);
                    System.out.println("Created new user: " + username + " (ID: " + userId + ")");
                    session.setCurrentUserId(userId);  // Save logged in user globally
                    app.showMainScreen();               // Switch screen
                } else {
                    System.out.println("User already exists, please login instead.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
