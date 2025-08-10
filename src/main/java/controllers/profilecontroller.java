package controllers;

import DAO.userdao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import utils.aiclientgemini;
import utils.fileuploader;
import utils.textextractor;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
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

    @FXML
    private void handleUploadResume() {
        // Pick files/folders from user
        List<Path> files = fileuploader.pickFiles();
        if (files.isEmpty()) {
            System.out.println("No files selected.");
            return;
        }

        // Extract text from selected files
        textextractor extractor = new textextractor();
        var result = extractor.extract(files);

        // Show extraction errors if any


        String resumeText = result.text();
        if (resumeText.isBlank()) {
            System.out.println("No text extracted from resume.");
            return;
        }

        // Define the keywords your AI expects to score
        List<String> keywords = List.of("Education", "ProgrammingSkills", "Certifications", "Projects", "Collaboration", "Experience");

        // Call AI client
        aiclientgemini aiClient = new aiclientgemini();
        try {
            Map<String, Integer> scores = aiClient.score(
                    String.valueOf(userId),   // userId as string
                    null,                     // jobId, if applicable
                    resumeText,
                    keywords
            );

            // Print scores or update UI
            System.out.println("Resume scores:");
            scores.forEach((k,v) -> System.out.println(k + ": " + v));

            // Update skills list in UI with the AI scores
            ObservableList<String> skillItems = FXCollections.observableArrayList();
            scores.forEach((k,v) -> skillItems.add(k + ": " + v + "%"));
            skillsList.setItems(skillItems);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("AI scoring failed: " + e.getMessage());
        }
    }
}
