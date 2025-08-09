package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class maincontroller {

    @FXML private TabPane maintabs;
    @FXML private Tab profileTab;
    @FXML private Tab jobsTab;

    // Called by the buttons in main.fxml
    @FXML
    private void goToProfile() {
        maintabs.getSelectionModel().select(profileTab);
    }

    @FXML
    private void goToJobs() {
        maintabs.getSelectionModel().select(jobsTab);
    }

    // Optional: Initialize method if needed
    @FXML
    private void initialize() {
        // Could preload stuff here if needed
    }
}
