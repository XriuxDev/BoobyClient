package com.boobyclient.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Professional Minecraft Launcher GUI - Similar to Feather Client
 */
public class ModernLauncherApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ModernLauncherApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Booby Client Modern Launcher");

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modern-launcher.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/modern-style.css").toExternalForm());

            // Setup stage
            primaryStage.setTitle("Booby Client - Minecraft Launcher");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Center on screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX((screenBounds.getWidth() - 1000) / 2);
            primaryStage.setY((screenBounds.getHeight() - 600) / 2);

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
