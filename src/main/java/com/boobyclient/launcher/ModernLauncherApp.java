package com.boobyclient.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting Booby Client Modern Launcher");
        showUpdateStage(primaryStage);

        Thread updateThread = new Thread(() -> {
            UpdateManager.UpdateResult result = UpdateManager.checkForUpdates();
            Platform.runLater(() -> handleUpdateResult(primaryStage, result));
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void showUpdateStage(Stage primaryStage) {
        Label label = new Label("Checking for updates...");
        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(280);
        bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        VBox content = new VBox(12, label, bar);
        content.setAlignment(Pos.CENTER);

        StackPane pane = new StackPane(content);
        pane.setPrefSize(420, 220);

        Scene scene = new Scene(pane);
        primaryStage.setTitle("Booby Client - Updating");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleUpdateResult(Stage primaryStage, UpdateManager.UpdateResult result) {
        if (result.updateLaunched) {
            logger.info("Update started, exiting launcher");
            Platform.exit();
            return;
        }

        if (result.updateRequiredFailed) {
            logger.error("Update required but failed: {}", result.errorMessage);
            Platform.exit();
            return;
        }

        if (result.errorMessage != null) {
            logger.warn("Update check failed: {}", result.errorMessage);
        }

        loadMainUi(primaryStage);
    }

    private void loadMainUi(Stage primaryStage) {
        try {
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
