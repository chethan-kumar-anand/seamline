package com.seamline.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    @FXML
    private StackPane designContainer;

    @FXML
    private ImageView svgPreview;

    @FXML
    private void onOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg"),
            new FileChooser.ExtensionFilter("DXF Files", "*.dxf")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            if (file.getName().endsWith(".svg")) {
                loadSVG(file);
            } else if (file.getName().endsWith(".dxf")) {
                loadDXF(file);
            }
        }
    }


    private void loadSVG(File file) {
        try {
            String svgContent = Files.readString(Paths.get(file.toURI()));
    
            // Debug: Print full SVG content
            // System.out.println("SVG Content: " + svgContent);
    
            String pathData = extractPathData(svgContent);
            if (pathData == null) {
                showError("Invalid SVG", "No valid <path> data found.");
                return;
            }
    
            // Debug: Print extracted path data
            // System.out.println("Extracted Path: " + pathData);
    
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(pathData);
            svgPath.setStyle("-fx-fill: black;");
    
            designContainer.getChildren().clear();
            designContainer.getChildren().add(svgPath);
        } catch (Exception e) {
            showError("Error Loading SVG", e.getMessage());
        }
    }

    private void loadDXF(File file) {
        try {
            org.kabeja.parser.Parser parser = new org.kabeja.parser.SAXParser();
            parser.parse(file.getAbsolutePath());
    
            org.kabeja.dxf.DXFDocument doc = parser.getDocument();
            org.kabeja.dxf.DXFLayer layer = doc.getDXFLayer(doc.getLayers().iterator().next()); // Load first layer
    
            designContainer.getChildren().clear();
    
            for (org.kabeja.dxf.DXFEntity entity : layer.getDXFEntities()) {
                if (entity instanceof org.kabeja.dxf.DXFLine) {
                    org.kabeja.dxf.DXFLine line = (org.kabeja.dxf.DXFLine) entity;
                    javafx.scene.shape.Line javafxLine = new javafx.scene.shape.Line(
                        line.getStartPoint().getX(), line.getStartPoint().getY(),
                        line.getEndPoint().getX(), line.getEndPoint().getY()
                    );
                    javafxLine.setStyle("-fx-stroke: black; -fx-stroke-width: 1;");
                    designContainer.getChildren().add(javafxLine);
                }
            }
        } catch (Exception e) {
            showError("Error Loading DXF", e.getMessage());
        }
    }
    

    private String extractPathData(String svgContent) {
        Pattern pattern = Pattern.compile("<path[^>]* d=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(svgContent);

        StringBuilder allPaths = new StringBuilder();
        while (matcher.find()) {
            allPaths.append(matcher.group(1)).append(" ");
        }

        return allPaths.length() > 0 ? allPaths.toString().trim() : null;
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
