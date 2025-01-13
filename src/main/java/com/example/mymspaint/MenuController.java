package com.example.mymspaint;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.scene.input.KeyEvent;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MenuController {

    @FXML
    private Canvas canvas;

    private Image loadedImage;

    @FXML
    protected void onOpenClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                loadedImage = new Image(selectedFile.toURI().toString());
                drawImageOnCanvas(loadedImage);
            } catch (Exception e) {
                showErrorAlert("Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onSaveClick() {
        if (loadedImage == null) {
            showErrorAlert("No image loaded to save!");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        File savedFile = fileChooser.showSaveDialog(null);
        if (savedFile != null) {
            try {
                var writableImage = new javafx.scene.image.WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", savedFile);
            } catch (IOException e) {
                showErrorAlert("Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onUndoClick() {
        System.out.println("Undo action triggered!");
    }

    @FXML
    protected void onAboutClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About MyMSPaint");
        alert.setContentText("This is a simple paint application created in JavaFX.");
        alert.showAndWait();
    }

    @FXML
    protected void onExitSaveClick() {
        onSaveClick(); // Uloží obrázek
        System.exit(0); // Ukončí aplikaci
    }

    @FXML
    protected void onExitClick() {
        System.exit(0); // Ukončí aplikaci bez uložení
    }

    @FXML
    protected void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case S -> {
                if (event.isControlDown()) {
                    onSaveClick(); // Ctrl+S pro uložení
                }
            }
            case O -> {
                if (event.isControlDown()) {
                    onOpenClick(); // Ctrl+O pro otevření
                }
            }
            case Z -> {
                if (event.isControlDown()) {
                    onUndoClick(); // Ctrl+Z pro undo
                }
            }
            case ESCAPE -> onExitClick(); // Esc pro ukončení
        }
    }


    private void drawImageOnCanvas(Image image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
