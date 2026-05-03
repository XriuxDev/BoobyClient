package com.boobyclient.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main launcher application entry point
 */
public class LauncherApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(LauncherApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Booby Client Launcher");

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/launcher.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            // Setup stage
            primaryStage.setTitle("Booby Client - Minecraft 1.21+ PvP Launcher");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Set icon if available
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                logger.warn("Could not load icon", e);
            }

            primaryStage.show();
            logger.info("Launcher started successfully");

        } catch (IOException e) {
            logger.error("Failed to load launcher UI", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
