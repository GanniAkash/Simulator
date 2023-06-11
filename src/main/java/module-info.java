module com.akash {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    opens com.akash to javafx.fxml;
    exports com.akash;
}
