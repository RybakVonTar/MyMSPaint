module com.example.mymspaint {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mymspaint to javafx.fxml;
    exports com.example.mymspaint;
}