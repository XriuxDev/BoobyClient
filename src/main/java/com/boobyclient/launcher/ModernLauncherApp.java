package com.boobyclient.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Professional Minecraft Launcher GUI - Similar to Feather Client
 */
public class ModernLauncherApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ModernLauncherApp.class);
    public static final String VERSION = "1.1.3"; // CHANGE THIS when you update!
    private static java.net.ServerSocket lockSocket;

    @Override
    public void start(Stage primaryStage) {
        if (!checkSingleInstance()) {
            Platform.exit();
            System.exit(0);
            return;
        }

        // Setup UI components for progress
        Label label = new Label("Checking for updates...");
        Label versionLabel = new Label("Version " + VERSION);
        versionLabel.getStyleClass().add("muted-text");
        
        ProgressBar bar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        bar.setPrefWidth(280);

        showUpdateStage(primaryStage, label, bar, versionLabel);

        Thread updateThread = new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {} // Minimum splash time
            
            UpdateManager.UpdateResult result = UpdateManager.checkForUpdates(progress -> {
                Platform.runLater(() -> {
                    label.setText(String.format("Downloading update... (%.0f%%)", progress * 100));
                    bar.setProgress(progress);
                });
            });
            Platform.runLater(() -> handleUpdateResult(primaryStage, result));
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showUpdateStage(Stage primaryStage, Label label, ProgressBar bar, Label versionLabel) {
        VBox content = new VBox(12, label, bar, versionLabel);
        content.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane(content);
        pane.setPrefSize(420, 220);

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("/css/modern-style.css").toExternalForm());
        primaryStage.setTitle("Booby Client - Updating");
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/icon.png")));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleUpdateResult(Stage primaryStage, UpdateManager.UpdateResult result) {
        if (result.updateLaunched) {
            logger.info("Update started, exiting launcher");
            LauncherLog.info("Update started, exiting launcher");
            Platform.exit();
            return;
        }

        if (result.updateRequiredFailed) {
            logger.error("Update required but failed: {}", result.errorMessage);
            LauncherLog.error("Update required but failed: " + result.errorMessage, null);
            showError("Update failed", "Update required but failed. Check launcher.log for details.");
            Platform.exit();
            return;
        }

        if (result.errorMessage != null) {
            logger.warn("Update check failed: {}", result.errorMessage);
            LauncherLog.warn("Update check failed: " + result.errorMessage);
            showError("Update check failed", "Update check failed, starting anyway.");
        }

        loadMainUi(primaryStage);
    }

    private void loadMainUi(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modern-launcher.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/modern-style.css").toExternalForm());
            primaryStage.setTitle("Booby Client - Minecraft Launcher");
            primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/icon.png")));
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // Center on screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX((screenBounds.getWidth() - 1000) / 2);
            primaryStage.setY((screenBounds.getHeight() - 600) / 2);

            primaryStage.show();
            logger.info("Launcher started successfully");
            LauncherLog.info("Launcher UI loaded");

        } catch (IOException e) {
            logger.error("Failed to load launcher UI", e);
            LauncherLog.error("Failed to load launcher UI", e);
            System.exit(1);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Booby Client");
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    private boolean checkSingleInstance() {
        try {
            // We use 25556 for the lock (25555 is used by the game socket server)
            lockSocket = new java.net.ServerSocket(25556);
            return true;
        } catch (Exception e) {
            logger.warn("Another instance is already running.");
            return false;
        }
    }

    @Override
    public void stop() {
        logger.info("Launcher stopping...");
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

