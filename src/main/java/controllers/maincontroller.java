package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Map;

public class maincontroller {
    @FXML private TabPane maintabs;
    @FXML private Tab profileTab;
    @FXML private Tab jobsTab;

    private int userId;
    private userdao userDao = new userdao();

    // Called when FXML loads
    @FXML
    private void initialize() {
        // You'll populate tabs here later
    }

    // Called from authcontroller
    public void setUserData(int userId, String userName) {
        this.userId = userId;
        System.out.println("User ID set: " + userId);

        // Example: Load user data when tab is selected
        profileTab.setOnSelectionChanged(event -> {
            if (profileTab.isSelected()) {
                loadProfileData();
            }
        });
    }

    private void loadProfileData() {
        try {
            // Example: Load user skills from database
            Map<String, Integer> skills = userDao.getSkills(userId);
            System.out.println("Loaded skills: " + skills);

            // You'll update UI here later
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}