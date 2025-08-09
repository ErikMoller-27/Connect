package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Arrays;

public class profilecontroller {
    @FXML private PieChart radialChart;
    @FXML private ListView<String> documentList;
    @FXML private TabPane mainTabPane;
    @FXML private ComboBox<String> industryComboBox;

    @FXML
    public void initialize() {
        // Initialize chart
        updateRadialChart(new double[]{80, 75, 60, 85, 70, 90});

        // Set up industry dropdown
        industryComboBox.getItems().addAll(
                "Technology",
                "Finance",
                "Education",
                "Healthcare",
                "Engineering"
        );
    }

    @FXML
    private void handleDocumentUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            documentList.getItems().add(file.getName());
        }
    }

    @FXML
    private void handleAnalysis() {
        // Simulate AI analysis
        double[] newScores = {
                Math.min(100, radialChart.getData().get(0).getPieValue() + 5),
                Math.min(100, radialChart.getData().get(1).getPieValue() + 3),
                // ... other scores
        };
        updateRadialChart(newScores);

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Analysis complete!");
        alert.show();
    }

    private void updateRadialChart(double[] values) {
        radialChart.getData().clear();
        String[] categories = {
                "Technical Skills",
                "Communication",
                "Leadership",
                "Creativity",
                "Industry Fit",
                "Problem Solving"
        };

        for (int i = 0; i < categories.length; i++) {
            radialChart.getData().add(new PieChart.Data(categories[i], values[i]));
        }
    }
}