package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import app.app; // Import the outer App class

public class authcontroller {
    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;

    private final userdao userDao = new userdao();

    @FXML
    public void initialize() {
        try {
            userDao.initializeTables(); // Make sure tables exist
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
                    app.MainApp.showMainScreen();
                } else {
                    System.out.println("User not found, please sign up first.");
                    // Optionally show an alert dialog here
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
                    app.MainApp.showMainScreen();
                } else {
                    System.out.println("User already exists, please login instead.");
                    // Optionally show an alert dialog here
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findOrCreateUser(String username) throws Exception {
        // For now, always create new (can change later to check DB)
        return userDao.createUser(username);
    }
}
