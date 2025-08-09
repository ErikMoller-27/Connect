package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.session;

public class logincontroller {

    @FXML private TextField usernameField;
    @FXML private Button loginButton;

    private userdao userDao = new userdao();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        try {
            int userId = userDao.getUserIdByName(username);
            if (userId > 0) {
                System.out.println("Logged in as existing user: " + username + " (ID: " + userId + ")");
                session.setCurrentUserId(userId);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                System.out.println("User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
