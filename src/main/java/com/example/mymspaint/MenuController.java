package com.example.mymspaint;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MenuController {

    @FXML
    private Canvas canvas;

    @FXML
    private AnchorPane rootPane;

    private Image loadedImage;
    private GraphicsContext gc;
    private final ArrayList<WritableImage> prevImageList = new ArrayList<>();

    @FXML
    protected void initialize() {
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        gc = canvas.getGraphicsContext2D();

        // Bind canvas size to root pane size
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> resizeCanvas());
    }

    private void resizeCanvas() {
        double newWidth = rootPane.getWidth();
        double newHeight = rootPane.getHeight();

        if (newWidth > 0 && newHeight > 0) {
            WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, snapshot);

            canvas.setWidth(newWidth);
            canvas.setHeight(newHeight);

            gc.clearRect(0, 0, newWidth, newHeight);
            gc.drawImage(snapshot, 0, 0, newWidth, newHeight);
        }
    }

    @FXML
    protected void onOpenClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
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
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", savedFile);
            } catch (IOException e) {
                showErrorAlert("Failed to save image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void onUndoClick() {
        if (prevImageList.isEmpty()) {
            showErrorAlert("Nothing to undo!");
            return;
        }
        WritableImage lastImage = prevImageList.remove(prevImageList.size() - 1);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(lastImage, 0, 0);
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

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(negatedImage, 0, 0);
    }

    @FXML
    protected void onThresholdClick() {
        if (loadedImage == null) {
            showErrorAlert("No Image Found");
            return;
        }

        Stage thresholdDialog = new Stage();
        thresholdDialog.initModality(Modality.APPLICATION_MODAL);
        thresholdDialog.setTitle("Threshold Adjustment");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        Label instructionLabel = new Label("Set Threshold Value (0.0 - 1.0):");
        TextField thresholdInput = new TextField("0.5");

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            try {
                double threshold = Double.parseDouble(thresholdInput.getText());
                if (threshold < 0.0 || threshold > 1.0) {
                    throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0");
                }
                applyThreshold(threshold);
                thresholdDialog.close();
            } catch (Exception ex) {
                showErrorAlert("Invalid threshold value. Please enter a number between 0.0 and 1.0.");
            }
        });

        dialogVBox.getChildren().addAll(instructionLabel, thresholdInput, applyButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        thresholdDialog.setScene(dialogScene);
        thresholdDialog.show();
    }

    private void applyThreshold(double threshold) {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);
        savePrevImage(writableImage);

        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage thresholdImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        PixelWriter pixelWriter = thresholdImage.getPixelWriter();

        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);
                double luminance = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                Color binaryColor = luminance > threshold ? Color.WHITE : Color.BLACK;
                pixelWriter.setColor(x, y, binaryColor);
            }
        }

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(thresholdImage, 0, 0);
    }

    @FXML
    protected void onRgbAdjustClick() {
        if (loadedImage == null) {
            showErrorAlert("No Image Found");
            return;
        }

        Stage rgbDialog = new Stage();
        rgbDialog.initModality(Modality.APPLICATION_MODAL);
        rgbDialog.setTitle("RGB Adjustment");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Red slider
        Label redLabel = new Label("Red:");
        Slider redSlider = new Slider(0, 255, 128);
        Label redValueLabel = new Label("128");

        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            redValueLabel.setText(String.valueOf(newVal.intValue()));
        });

        // Green slider
        Label greenLabel = new Label("Green:");
        Slider greenSlider = new Slider(0, 255, 128);
        Label greenValueLabel = new Label("128");

        greenSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            greenValueLabel.setText(String.valueOf(newVal.intValue()));
        });

        // Blue slider
        Label blueLabel = new Label("Blue:");
        Slider blueSlider = new Slider(0, 255, 128);
        Label blueValueLabel = new Label("128");

        blueSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            blueValueLabel.setText(String.valueOf(newVal.intValue()));
        });

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            applyRgbAdjust((int) redSlider.getValue(), (int) greenSlider.getValue(), (int) blueSlider.getValue());
            rgbDialog.close();
        });

        dialogVBox.getChildren().addAll(
                redLabel, redSlider, redValueLabel,
                greenLabel, greenSlider, greenValueLabel,
                blueLabel, blueSlider, blueValueLabel,
                applyButton
        );

        Scene dialogScene = new Scene(dialogVBox, 300, 400);
        rgbDialog.setScene(dialogScene);
        rgbDialog.show();
    }

    private void applyRgbAdjust(int redAdjust, int greenAdjust, int blueAdjust) {
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);
        savePrevImage(writableImage);

        PixelReader pixelReader = writableImage.getPixelReader();
        WritableImage adjustedImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        PixelWriter pixelWriter = adjustedImage.getPixelWriter();

        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                Color color = pixelReader.getColor(x, y);

                double red = Math.min(Math.max(color.getRed() * 255 + redAdjust - 128, 0), 255) / 255.0;
                double green = Math.min(Math.max(color.getGreen() * 255 + greenAdjust - 128, 0), 255) / 255.0;
                double blue = Math.min(Math.max(color.getBlue() * 255 + blueAdjust - 128, 0), 255) / 255.0;

                Color adjustedColor = Color.color(red, green, blue, color.getOpacity());
                pixelWriter.setColor(x, y, adjustedColor);
            }
        }

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(adjustedImage, 0, 0);
    }

    @FXML
    protected void onAboutClick() {
        Stage newWindow = new Stage();

        VBox newRoot = new VBox(10);
        newRoot.setStyle("-fx-padding: 10; -fx-alignment: center;");
        newRoot.getChildren().add(new Label("MyMSPaint\nAuthor: Your Name\nVersion: 1.0"));

        Scene newScene = new Scene(newRoot, 300, 150);
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

    private void onKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case S -> onSaveClick();
                case Z -> onUndoClick();
                case O -> onOpenClick();
                default -> {}
            }
        }
    }

    private void drawImageOnCanvas(Image image) {
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