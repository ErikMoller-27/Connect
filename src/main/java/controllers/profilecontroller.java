package controllers;

import DAO.userdao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import utils.aiclientgemini;
import utils.fileuploader;
import utils.fxradarwindow;
import utils.textextractor;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class profilecontroller {

    @FXML private Label nameLabel;
    @FXML private ListView<String> skillsList;
    @FXML private Button uploadResumeButton;

    private final userdao userDao = new userdao();
    private int userId;

    // Load user data for a given userId (called by whoever navigates to Profile)
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
        // 1) Pick files (Swing chooser you already have)
        List<Path> files = fileuploader.pickFiles();
        if (files.isEmpty()) {
            System.out.println("No files selected.");
            return;
        }

        // disable button while processing
        uploadResumeButton.setDisable(true);
        uploadResumeButton.setText("Processing…");

        // 2) Heavy work off the UI thread
        Task<Map<String,Integer>> task = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                // Extract
                textextractor extractor = new textextractor();
                var result = extractor.extract(files);

                // Log any per-file errors (non-fatal)
                result.files().stream()
                        .filter(f -> !f.succeeded())
                        .forEach(f -> System.out.println("Error reading " + f.name + ": " + f.error));

                String resumeText = result.text();
                if (resumeText.isBlank()) {
                    throw new IllegalArgumentException("No text extracted from resume.");
                }

                // 3) AI call (fixed 6 keys)
                List<String> keys = List.of(
                        "Education","ProgrammingSkills","Certifications",
                        "Projects","Collaboration","Experience"
                );
                aiclientgemini ai = new aiclientgemini();
                Map<String,Integer> raw = ai.score(String.valueOf(userId), null, resumeText, keys);

                // 4) Stable ordered map with all six keys
                Map<String,Integer> six = new LinkedHashMap<>();
                six.put("Education",          raw.getOrDefault("Education", 0));
                six.put("ProgrammingSkills",  raw.getOrDefault("ProgrammingSkills", 0));
                six.put("Certifications",     raw.getOrDefault("Certifications", 0));
                six.put("Projects",           raw.getOrDefault("Projects", 0));
                six.put("Collaboration",      raw.getOrDefault("Collaboration", 0));
                six.put("Experience",         raw.getOrDefault("Experience", 0));

                // 5) Persist (no resumes stored—just key/value scores)
                userDao.initializeTables();
                userDao.replaceSkills(userId, six);

                return six;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String,Integer> six = task.getValue();

            // Update list UI
            ObservableList<String> items = FXCollections.observableArrayList();
            six.forEach((k,v) -> items.add(k + ": " + v + "%"));
            skillsList.setItems(items);

            // re-enable button
            uploadResumeButton.setDisable(false);
            uploadResumeButton.setText("Upload Resume");

            // Show animated radar in a background thread so FX thread stays free
            new Thread(() -> fxradarwindow.showAndWait(six), "radar-modal").start();
        });

        task.setOnFailed(e -> {
            // re-enable button
            uploadResumeButton.setDisable(false);
            uploadResumeButton.setText("Upload Resume");

            var ex = task.getException();
            if (ex != null) ex.printStackTrace();
            alert("Upload failed", (ex != null ? ex.getMessage() : "Unknown error"));
        });

        new Thread(task, "resume-upload-pipeline").start();
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
