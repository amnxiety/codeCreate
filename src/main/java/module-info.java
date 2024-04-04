module com.example.new_bounce {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;


    opens com.example.new_bounce to javafx.fxml;
    exports com.example.new_bounce;
}