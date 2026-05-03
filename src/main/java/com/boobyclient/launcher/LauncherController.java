package com.boobyclient.launcher;

import com.boobyclient.util.GameLauncher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the main launcher UI
 */
public class LauncherController {
    private static final Logger logger = LoggerFactory.getLogger(LauncherController.class);

    @FXML private VBox mainContainer;
    @FXML private ComboBox<String> versionSelector;
    @FXML private ComboBox<String> accountSelector;
    @FXML private CheckBox toggleSprintCheckbox;
    @FXML private CheckBox fpsCounterCheckbox;
    @FXML private CheckBox pingDisplayCheckbox;
    @FXML private CheckBox comboCounterCheckbox;
    @FXML private Button launchButton;
    @FXML private Button settingsButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar launchProgress;

    private GameLauncher gameLauncher;

    @FXML
    public void initialize() {
        logger.info("Initializing launcher controller");
        gameLauncher = new GameLauncher();

        // Setup version selector
        versionSelector.getItems().addAll("1.21", "1.21.1", "1.20.4", "1.20.1");
        versionSelector.setValue("1.21");

        // Setup account selector
        accountSelector.getItems().addAll("Player1", "Player2", "Player3");
        accountSelector.setValue("Player1");

        // Setup HUD checkboxes with defaults
        toggleSprintCheckbox.setSelected(true);
        fpsCounterCheckbox.setSelected(true);
        pingDisplayCheckbox.setSelected(true);
        comboCounterCheckbox.setSelected(true);

        // Setup launch button
        launchButton.setOnAction(e -> handleLaunch());
        settingsButton.setOnAction(e -> handleSettings());

        updateStatus("Ready to launch", false);
        logger.info("Launcher controller initialized");
    }

    @FXML
    private void handleLaunch() {
        String selectedVersion = versionSelector.getValue();
        String selectedAccount = accountSelector.getValue();

        logger.info("Launching Minecraft {} with account: {}", selectedVersion, selectedAccount);
        updateStatus("Starting Minecraft " + selectedVersion + "...", true);
        launchButton.setDisable(true);

        // Create launcher configuration
        GameLauncher.LaunchConfig config = new GameLauncher.LaunchConfig();
        config.version = selectedVersion;
        config.account = selectedAccount;
        config.hudModules = new GameLauncher.HUDConfig(
            toggleSprintCheckbox.isSelected(),
            fpsCounterCheckbox.isSelected(),
            pingDisplayCheckbox.isSelected(),
            comboCounterCheckbox.isSelected()
        );

        // Launch in background thread
        Thread launchThread = new Thread(() -> {
            try {
                gameLauncher.launch(config);
                updateStatus("Game launched successfully!", false);
            } catch (Exception e) {
                logger.error("Failed to launch game", e);
                updateStatus("Error: " + e.getMessage(), false);
                launchButton.setDisable(false);
            }
        });
        launchThread.setDaemon(true);
        launchThread.start();
    }

    @FXML
    private void handleSettings() {
        logger.info("Opening settings dialog");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Booby Client Settings");
        alert.setContentText("Settings panel coming soon!");
        alert.showAndWait();
    }

    private void updateStatus(String message, boolean isLoading) {
        statusLabel.setText(message);
        launchProgress.setVisible(isLoading);
        if (isLoading) {
            launchProgress.setStyle("-fx-accent: #00ff00;");
        }
    }
}
