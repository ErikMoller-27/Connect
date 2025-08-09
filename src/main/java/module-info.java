module com.example.connect {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.connect to javafx.fxml;
    exports com.example.connect;
}