package controllers;

import DAO.userdao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import utils.session;

import java.sql.SQLException;
import java.util.Map;

public class profilecontroller {

    @FXML private Label nameLabel;
    @FXML private ListView<String> skillsList;

    private userdao userDao = new userdao();

    @FXML
    public void initialize() {
        int userId = session.getCurrentUserId();
        System.out.println("Profile controller initialized with userId = " + userId);
        if (userId == -1) {
            nameLabel.setText("No valid user loaded! Cannot load profile.");
            System.out.println("No logged in user in session.");
            return;
        }

        try {
            var profile = userDao.getUserProfile(userId);
            nameLabel.setText(profile.getFirstName());

            ObservableList<String> skillItems = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : profile.getSkills().entrySet()) {
                skillItems.add(entry.getKey() + ": " + entry.getValue() + "%");
            }
            skillsList.setItems(skillItems);

        } catch (SQLException e) {
            e.printStackTrace();
            nameLabel.setText("Error loading user");
        }
    }

    // For brevity, upload resume code omitted here, can be added as needed
}
