package controllers;

import DAO.userdao;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.Map;

public class profilecontroller {

    @FXML private Label nameLabel;
    @FXML private ListView<String> skillsList;

    private userdao userDao = new userdao();
    private int userId;

    // Call this to load user data for a given userId
    public void loadUserProfile(int userId) {
        this.userId = userId;
        try {
            var userProfile = userDao.getUserProfile(userId);
            nameLabel.setText(userProfile.getFirstName());

            ObservableList<String> skillItems = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : userProfile.getSkills().entrySet()) {
                skillItems.add(entry.getKey() + ": " + entry.getValue() + "%");
            }
            skillsList.setItems(skillItems);

        } catch (SQLException e) {
            e.printStackTrace();
            nameLabel.setText("Error loading user");
        }
    }
}
