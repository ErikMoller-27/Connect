package controllers;

import DAO.userdao;
import utils.AIClientWrapper;
import utils.fileuploader;
import utils.textextractor;
import utils.textextractor.ExtractionResult;
import utils.RadarView;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class profilecontroller {
    @FXML private Label nameLabel;
    @FXML private ListView<String> skillsList;
    @FXML private RadarView radarView;

    private final userdao userDao = new userdao();
    private final AIClientWrapper aiClient = new AIClientWrapper();

    private int currentUserId = -1;

    public void loadUserProfile(int userId) {
        this.currentUserId = userId;
        try {
            var profile = userDao.getUserProfile(userId);
            nameLabel.setText(profile.getFirstName());

            skillsList.getItems().clear();
            Map<String, Integer> skills = profile.getSkills();
            for (var entry : skills.entrySet()) {
                skillsList.getItems().add(entry.getKey() + ": " + entry.getValue() + "%");
            }

            radarView.setScores(skills);
            radarView.play();

        } catch (Exception e) {
            e.printStackTrace();
            nameLabel.setText("Error loading user");
        }
    }

    @FXML
    private void handleUploadResume() {
        if (currentUserId == -1) {
            System.out.println("No user loaded - cannot upload resume.");
            return;
        }

        try {
            List<Path> files = fileuploader.pickFiles();
            if (files.isEmpty()) {
                System.out.println("No files selected.");
                return;
            }

            textextractor extractor = new textextractor();
            ExtractionResult result = extractor.extract(files);

            if (result.text().isBlank()) {
                System.out.println("No text extracted from files.");
                return;
            }

            List<String> keywords = List.of("Education", "ProgrammingSkills", "Certifications", "Projects", "Collaboration", "Experience");
            Map<String, Integer> scores = aiClient.getScores(String.valueOf(currentUserId), null, result.text(), keywords);

            System.out.println("Resume scores: " + scores);

            userDao.replaceAISkills(currentUserId, scores);

            loadUserProfile(currentUserId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
