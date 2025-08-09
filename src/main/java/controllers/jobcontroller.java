import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;

public class jobcontroller {
    @FXML private ListView<String> joblist;
    @FXML private TextField searchfield;

    @FXML
    public void initialize() {
        joblist.setItems(FXCollections.observableArrayList(
                "software engineer at tanda - 85% match",
                "data analyst at macquarie - 76% match",
                "ux researcher at qut - 68% match"
        ));
    }

    @FXML
    private void handlesearch() {
        String query = searchfield.getText().toLowerCase();
        joblist.getItems().filtered(job ->
                job.toLowerCase().contains(query)
        );
    }
}