package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;

public class jobcontroller {
    @FXML private ListView<String> jobsList;
    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        // Sample job data
        jobsList.setItems(FXCollections.observableArrayList(
                "Software Engineer at Tanda - Match: 85%",
                "Data Analyst at Macquarie - Match: 76%",
                "UX Researcher at QUT - Match: 68%"
        ));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        jobsList.getItems().filtered(job ->
                job.toLowerCase().contains(query)
        );
    }
}