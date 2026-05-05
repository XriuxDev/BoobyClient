package com.boobyclient.launcher;

import com.boobyclient.util.GameLauncher;
import com.boobyclient.util.GameInstaller;
import com.boobyclient.util.FabricInjector;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for modern launcher UI (Feather Client style)
 * Handles authentication, version selection, and game launching
 */
public class ModernLauncherController {
    private static final Logger logger = LoggerFactory.getLogger(ModernLauncherController.class);

    // Main screens
    @FXML private StackPane contentPane;
    @FXML private VBox homeScreen;
    @FXML private VBox settingsScreen;
    @FXML private VBox profilesScreen;

    // Home screen controls
    @FXML private VBox loginSection;
    @FXML private Label usernameLabel;
    @FXML private Button loginButton;
    @FXML private Button logoutButton;

    @FXML private ComboBox<String> versionSelector;
    @FXML private Button launchButton;
    @FXML private Label statusLabel;
    @FXML private Label clientVersionLabel;
    @FXML private ProgressBar launchProgress;

    // Settings screen controls
    @FXML private ComboBox<String> memorySelector;
    @FXML private ComboBox<String> themeSelector;
    @FXML private Button saveSettingsButton;

    // Profiles screen controls
    @FXML private TextField profileNameField;
    @FXML private ListView<String> profilesList;

    // Authentication and game management
    private MinecraftAuthManager minecraftAuth;
    private SocketServer socketServer;
    private GameLauncher gameLauncher;
    private boolean isGameRunning = false;

    @FXML
    public void initialize() {
        logger.info("Initializing modern launcher controller");
        if (clientVersionLabel != null) {
            clientVersionLabel.setText(ModernLauncherApp.VERSION);
        }

        // Initialize managers
        minecraftAuth = new MinecraftAuthManager();
        socketServer = new SocketServer();
        gameLauncher = new GameLauncher();

        // Setup UI based on authentication state
        setupAuthenticationUI();

        // Populate version selector
        versionSelector.getItems().addAll("1.21.11", "1.8.9");
        versionSelector.setValue("1.21.11");

        // Populate memory selector
        memorySelector.getItems().addAll("2G", "4G", "6G", "8G");
        memorySelector.setValue("4G");

        // Populate theme selector
        themeSelector.getItems().addAll("Dark (Default)", "Light", "Hacker");
        themeSelector.setValue("Dark (Default)");

        // Setup buttons
        launchButton.setOnAction(e -> launchGame());
        loginButton.setOnAction(e -> handleLogin());
        logoutButton.setOnAction(e -> handleLogout());

        // Setup socket server callbacks
        socketServer.setOnModConnected(() -> Platform.runLater(() -> {
            updateStatus("Mod connected!");
            isGameRunning = true;
            launchButton.setDisable(true);
        }));

        socketServer.setOnModDisconnected(() -> Platform.runLater(() -> {
            updateStatus("Game closed");
            isGameRunning = false;
            launchButton.setDisable(false);
        }));

        // Start socket server
        socketServer.start();

        // Show home screen by default
        showHome();

        logger.info("Modern launcher controller initialized");
    }

    /**
     * Setup authentication UI based on current state
     */
    private void setupAuthenticationUI() {
        if (minecraftAuth.isAuthenticated()) {
            // User is logged in
            String username = minecraftAuth.getUsername();
            usernameLabel.setText("Logged in as: " + username);
            loginButton.setVisible(false);
            logoutButton.setVisible(true);
            versionSelector.setDisable(false);
            launchButton.setDisable(false);
            updateStatus("Ready to launch");
            logger.info("User authenticated: {}", username);
        } else {
            // User needs to login
            usernameLabel.setText("Not logged in");
            loginButton.setVisible(true);
            loginButton.setText("LOGIN WITH MICROSOFT");
            logoutButton.setVisible(false);
            versionSelector.setDisable(true);
            launchButton.setDisable(true);
            updateStatus("Click LOGIN to authenticate");
            logger.info("User not authenticated");
        }
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        logger.info("Starting Minecraft authentication");
        updateStatus("Opening authentication browser...");
        loginButton.setDisable(true);

        // Run authentication in background
        Thread authThread = new Thread(() -> {
            boolean success = minecraftAuth.authenticate();
            Platform.runLater(() -> {
                if (success) {
                    logger.info("Authentication successful");
                    updateStatus("Authentication successful!");
                    setupAuthenticationUI();
                } else {
                    logger.warn("Authentication failed");
                    updateStatus("Authentication failed. Try again.");
                    loginButton.setDisable(false);
                }
            });
        });
        authThread.setDaemon(true);
        authThread.start();
    }

    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout() {
        logger.info("User logging out");
        minecraftAuth.logout();
        updateStatus("Logged out");
        setupAuthenticationUI();
    }

    /**
     * Launch game with selected version
     */
    @FXML
    public void launchGame() {
        if (!minecraftAuth.isAuthenticated()) {
            updateStatus("Error: Not authenticated");
            return;
        }

        String version = versionSelector.getValue();
        String username = minecraftAuth.getUsername();

        logger.info("Launching Minecraft {} as {}", version, username);
        updateStatus("Starting Minecraft " + version + "...");
        launchButton.setDisable(true);

        // Create config with Minecraft auth
        GameLauncher.LaunchConfig config = new GameLauncher.LaunchConfig();
        config.version = version;
        config.account = username;
        config.uuid = minecraftAuth.getUUID();
        config.authToken = minecraftAuth.getAccessToken();
        config.launcherSocket = "localhost:25555";
        config.hudModules = new GameLauncher.HUDConfig(true, true, true, true);

        // Launch in background
        Thread launchThread = new Thread(() -> {
            try {
                String mcDir = GameLauncher.MINECRAFT_DIR;
                
                // 1. Install Vanilla
                GameInstaller installer = new GameInstaller(mcDir);
                installer.installVersion(version, this::updateStatus).join();
                
                // 2. Inject Fabric
                FabricInjector injector = new FabricInjector(mcDir);
                String fabricApiVersion = version.equals("26.1.2") ? "1.21.11" : version;
                String profileName = injector.injectFabric(fabricApiVersion, version, this::updateStatus).join();
                
                // 3. Launch Fabric Profile
                config.version = profileName; // Launch the generated fabric profile
                
                updateStatus("Starting Game Engine...");
                gameLauncher.launch(config);
                updateStatus("Game closed.");
                logger.info("Game closed");
                
                // --- AUTO-CLEANUP ---
                try {
                    java.io.File modFile = new java.io.File(mcDir, "mods/booby-mod.jar");
                    if (modFile.exists()) {
                        modFile.delete();
                        logger.info("Auto-Cleanup: Removed booby-mod.jar");
                    }
                } catch (Exception e) {
                    logger.error("Auto-Cleanup failed", e);
                }
                
                Platform.runLater(() -> launchButton.setDisable(false));
            } catch (Exception e) {
                logger.error("Failed to launch game", e);
                updateStatus("Error: " + e.getMessage());
                Platform.runLater(() -> launchButton.setDisable(false));
            }
        });
        launchThread.setDaemon(true);
        launchThread.start();
    }

    /**
     * Update status label
     */
    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            logger.debug("Status: {}", message);
        });
    }

    // Navigation methods
    @FXML
    public void showHome() {
        logger.info("Switching to Home screen");
        homeScreen.toFront();
        homeScreen.setVisible(true);
        settingsScreen.setVisible(false);
        profilesScreen.setVisible(false);
    }

    @FXML
    public void showSettings() {
        logger.info("Switching to Settings screen");
        settingsScreen.toFront();
        homeScreen.setVisible(false);
        settingsScreen.setVisible(true);
        profilesScreen.setVisible(false);
    }

    @FXML
    public void showProfiles() {
        logger.info("Switching to Profiles screen");
        profilesScreen.toFront();
        homeScreen.setVisible(false);
        settingsScreen.setVisible(false);
        profilesScreen.setVisible(true);
        loadProfilesList();
    }

    @FXML
    private void saveSettings() {
        logger.info("Saving settings");
        
        // Save logic would go here
        
        // UI Animation instead of popup
        String originalText = saveSettingsButton.getText();
        String originalStyle = saveSettingsButton.getStyle();
        
        saveSettingsButton.setText("Saved!");
        saveSettingsButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(16, 185, 129, 0.4), 10, 0, 0, 4);");
        
        // Reset after 1.5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}
            
            Platform.runLater(() -> {
                saveSettingsButton.setText(originalText);
                saveSettingsButton.setStyle(originalStyle);
            });
        }).start();
    }

    @FXML
    private void createProfile() {
        String profileName = profileNameField.getText();
        if (profileName == null || profileName.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Profile name required");
            alert.showAndWait();
            return;
        }
        logger.info("Creating profile: {}", profileName);
        profileNameField.clear();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Profile Created");
        alert.setContentText("Profile '" + profileName + "' has been created!");
        alert.showAndWait();
    }

    @FXML
    private void loadProfile() {
        String selected = profilesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            logger.info("Loading profile: {}", selected);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Profile Loaded");
            alert.setContentText("Profile '" + selected + "' has been loaded!");
            alert.showAndWait();
        }
    }

    @FXML
    private void deleteProfile() {
        String selected = profilesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            logger.info("Deleting profile: {}", selected);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Profile");
            alert.setContentText("Are you sure you want to delete '" + selected + "'?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                logger.info("Profile deleted");
            }
        }
    }

    private void loadProfilesList() {
        // Placeholder - would load from ConfigManager
        profilesList.getItems().clear();
        profilesList.getItems().addAll("Default", "PvP", "Survival");
    }

    /**
     * Cleanup on app close
     */
    public void shutdown() {
        logger.info("Shutting down launcher");
        socketServer.stop();
    }
}
