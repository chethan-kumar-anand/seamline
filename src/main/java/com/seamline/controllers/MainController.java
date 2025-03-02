package com.seamline.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainController {
    @FXML
    private StackPane designContainer;

    @FXML
    private void onOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            loadSVG(file);
        }
    }

    private void loadSVG(File file) {
        try {
            String svgContent = Files.readString(Paths.get(file.toURI()));

            // Extract SVG path data (assuming a simple <path d="M10,10..."/> structure)
            String pathData = extractPathData(svgContent);
            if (pathData == null) {
                showError("Invalid SVG", "No valid <path> data found.");
                return;
            }

            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            svgPath.setStyle("-fx-fill: black;");

            designContainer.getChildren().clear();
            designContainer.getChildren().add(svgPath);
        } catch (Exception e) {
            showError("Error Loading SVG", e.getMessage());
        }
    }

    private String extractPathData(String svgContent) {
        int start = svgContent.indexOf("d=\"");
        if (start == -1) return null;
        start += 3;
        int end = svgContent.indexOf("\"", start);
        return end != -1 ? svgContent.substring(start, end) : null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
