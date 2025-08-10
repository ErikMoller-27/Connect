package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.session;

public class authcontroller {

    @FXML private TextField usernameField;
    @FXML private Button loginButton;   // just used to grab the Stage
    @FXML private Button signupButton;

    private final userdao userDao = new userdao();

    @FXML
    public void initialize() {
        try {
            userDao.initializeTables();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "DB init failed: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleLogin() {
        String name = clean(usernameField.getText());
        if (name.isBlank()) {
            info("Please enter a username.");
            return;
        }
        try {
            int userId = userDao.getUserIdByName(name);
            if (userId < 0) {
                info("No such user. Try Sign Up.");
                return;
            }
            goToProfile(userId);
        } catch (Exception e) {
            e.printStackTrace();
            error("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        String name = clean(usernameField.getText());
        if (name.isBlank()) {
            info("Please enter a username.");
            return;
        }
        try {
            int existing = userDao.getUserIdByName(name);
            int userId = (existing >= 0) ? existing : userDao.createUser(name);
            goToProfile(userId);
        } catch (Exception e) {
            e.printStackTrace();
            error("Sign up failed: " + e.getMessage());
        }
    }

    private void goToProfile(int userId) throws Exception {
        session.setCurrentUserId(userId);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
        Parent root = loader.load();

        profilecontroller pc = loader.getController();
        pc.loadUserProfile(userId);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
    private void info(String msg)  { new Alert(Alert.AlertType.INFORMATION, msg).showAndWait(); }
    private void error(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
