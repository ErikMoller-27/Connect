package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class maincontroller {
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to RadialMatch!");
    }
}