package com.example.mymspaint;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MenuController {

    @FXML
    private Canvas canvas;

    private Image loadedImage;
    public GraphicsContext gc;
    public ArrayList<WritableImage> prevImageList = new ArrayList<>();
    WritableImage prevImage;

    @FXML
    protected void onOpenClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                loadedImage = new Image(selectedFile.toURI().toString());
                drawImageOnCanvas(loadedImage);

                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                savePrevImage(writableImage);
            } catch (Exception e) {
                showErrorAlert("Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onSaveClick() {
        if (loadedImage == null) {
            showErrorAlert("No Image Found");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
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
        try {
            prevImage = prevImageList.getLast();
            prevImageList.remove(prevImage);

            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(prevImage, 0, 0);
        }
        catch (Exception e) {
            showErrorAlert("Nothing to undo!");
        }
    }

    @FXML
    protected void onNegationClick() {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);

        savePrevImage(writableImage);

        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage negatedImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        PixelWriter pixelWriter = negatedImage.getPixelWriter();

        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                Color negatedColor = Color.color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getOpacity());
                pixelWriter.setColor(x, y, negatedColor);
            }
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(negatedImage, 0, 0);
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
        System.exit(0);
    }

    @FXML
    protected void onExitClick() {
        System.exit(0);
    }


/*         NEJEDE TO

    protected void onKeyPressed(KeyEvent event) {
        final Set<KeyCode> pressedKeys = new HashSet<>();
        pressedKeys.add(event.getCode());

        if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.S)) {
            onSaveClick();
        }
        if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.Z)) {
            onUndoClick();
        }
        if (pressedKeys.contains(KeyCode.CONTROL) && pressedKeys.contains(KeyCode.O)) {
            onOpenClick();
        }
    }
 */

    private void drawImageOnCanvas(Image image) {
        gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void savePrevImage(WritableImage image) {
        prevImageList.add(image);
    }
}
