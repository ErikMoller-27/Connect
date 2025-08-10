package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class maincontroller {

    @FXML private TabPane maintabs;
    @FXML private Tab profileTab; // Make sure your main.fxml has fx:id="profileTab" on the profile Tab

    // Called when "Go to Profile" button is clicked
    @FXML
    private void goToProfile() {
        maintabs.getSelectionModel().select(profileTab);
    }

    // Called when "Go to Jobs" button is clicked
    @FXML
    private void goToJobs() {
        maintabs.getSelectionModel().select(1); // assuming Jobs tab is second
    }

    // Call this from app.MainApp after login to load profile for the user
    public void loadUserProfile(int userId) {
        try {
            // Load profile.fxml and set it as content of the profile tab
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/profile.fxml"));
            profileTab.setContent(loader.load());

            // Get the profilecontroller instance and call loadUserProfile(userId)
            profilecontroller profileCtrl = loader.getController();
            profileCtrl.loadUserProfile(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
