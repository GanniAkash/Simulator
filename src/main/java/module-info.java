module com.akash {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.akash to javafx.fxml;
    exports com.akash;
}
