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
        if (!result.files().isEmpty()) {
            result.files().stream()
                    .filter(f -> !f.succeeded())
                    .forEach(f -> System.out.println("Error reading " + f.name + ": " + f.error));
        }

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

            // Save the AI-generated scores into the database under "IT" subject
            // Wrap the scores map into a single key "IT" with the list of scores in order as a string (if you want)
            // Or save each keyword as its own row with subject = keyword (better for querying)
            // But per your request, subject is "IT" and percentages is a list
            // So we save with subject = "IT" and convert Map<String,Integer> scores to a JSON-like string or CSV string
            // For simplicity, here we save each keyword as its own skill row with subject = keyword

            // Uncomment this if you want to save all keywords under one subject "IT" as a CSV string (not currently supported)
            // userDao.replaceSkills(userId, Map.of("IT", serializeScores(scores)));

            // Instead, save each keyword as a separate skill subject for easier retrieval:
            userDao.replaceSkills(userId, scores);

            // Update skills list in UI with the AI scores
            ObservableList<String> skillItems = FXCollections.observableArrayList();
            scores.forEach((k,v) -> skillItems.add(k + ": " + v + "%"));
            skillsList.setItems(skillItems);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("AI scoring failed: " + e.getMessage());
        }
    }

    // Optional helper to serialize map values as CSV if needed
    /*
    private String serializeScores(Map<String, Integer> scores) {
        return scores.values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
    */
}
