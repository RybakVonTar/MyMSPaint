module com.example.mymspaint {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.mymspaint to javafx.fxml;
    exports com.example.mymspaint;
}