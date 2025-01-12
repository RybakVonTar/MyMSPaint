package com.example.mymspaint;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class MenuController {
    @FXML
    protected void onKeyPressed(KeyEvent event) {
        final Set<KeyCode> pressedKeys = new HashSet<>();
        pressedKeys.add(event.getCode());

        if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.S)) {
            onSaveClick();
        }
        if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.Z)) {
            onUndoClick();
        }
    }

    @FXML
    protected void onOpenClick() {
    }

    @FXML
    protected void onSaveClick() {
    }

    @FXML
    protected void onUndoClick() {
    }

    @FXML
    protected void onAboutClick() {
        Stage newWindow = new Stage();

        StackPane newRoot = new StackPane();
        newRoot.getChildren().add(new Text(""));

        Scene newScene = new Scene(newRoot, 256, 144);
        newWindow.setTitle("About");
        newWindow.setScene(newScene);
        newWindow.show();
    }

    @FXML
    protected void onExitSaveClick() {
        onSaveClick();
        onAboutClick();
    }

    @FXML
    protected void onExitClick() {
        Platform.exit();
    }
}