package com.boobyclient.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinecraftAuthManager {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftAuthManager.class);

    private static final String DEVICE_CODE_URL = "https://login.live.com/oauth20_connect.srf";
    private static final String TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    private static final String MINECRAFT_AUTH_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String XBOX_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String XBOX_XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";

    private static final String CLIENT_ID = "00000000402b5328";
    private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
    private static final String AUTH_TOKEN_FILE = System.getProperty("user.home") + "/.boobyclient/minecraft_auth.json";

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    private String accessToken;
    private String refreshToken;
    private String username;
    private String uuid;
    private long expiresAt;
    private boolean authenticated = false;

    public MinecraftAuthManager() {
        loadCachedToken();
    }

    public boolean authenticate() {
        logger.info("Starting device code authentication");
        try {
            JsonObject deviceCodeResponse = requestDeviceCode();
            if (deviceCodeResponse == null) {
                logger.error("Failed to get device code");
                return false;
            }

            String deviceCode = deviceCodeResponse.get("device_code").getAsString();
            String userCode = deviceCodeResponse.get("user_code").getAsString();
            String verificationUri = deviceCodeResponse.has("verification_uri")
                    ? deviceCodeResponse.get("verification_uri").getAsString()
                    : deviceCodeResponse.get("verification_url").getAsString();
            int interval = deviceCodeResponse.get("interval").getAsInt();
            int expiresIn = deviceCodeResponse.get("expires_in").getAsInt();

            AtomicBoolean dialogClosed = new AtomicBoolean(false);
            Stage[] dialogRef = new Stage[1];

            showDeviceCodeDialog(userCode, verificationUri, dialogClosed, dialogRef);

            long startTime = System.currentTimeMillis();
            long timeout = expiresIn * 1000L;

            JsonObject tokenResponse = null;
            while (System.currentTimeMillis() - startTime < timeout && !dialogClosed.get()) {
                Thread.sleep(interval * 1000L);

                tokenResponse = pollForToken(deviceCode);
                if (tokenResponse != null && tokenResponse.has("access_token")) {
                    break;
                }
                tokenResponse = null;
            }

            Platform.runLater(() -> {
                if (dialogRef[0] != null && dialogRef[0].isShowing()) {
                    dialogRef[0].close();
                }
            });

            if (tokenResponse == null) {
                logger.warn("Authentication timed out or was cancelled");
                return false;
            }

            String msAccessToken = tokenResponse.get("access_token").getAsString();
            if (tokenResponse.has("refresh_token")) {
                refreshToken = tokenResponse.get("refresh_token").getAsString();
            }
            expiresAt = System.currentTimeMillis() + (tokenResponse.get("expires_in").getAsInt() * 1000L);

            String xblToken = getXboxLiveToken(msAccessToken);
            if (xblToken == null) return false;

            JsonObject xstsData = getXSTSToken(xblToken);
            if (xstsData == null) return false;
            String xstsToken = xstsData.get("Token").getAsString();
            String userHash = xstsData.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

            String mcToken = getMinecraftToken(xstsToken, userHash);
            if (mcToken == null) return false;

            accessToken = mcToken;

            if (getMinecraftProfile()) {
                authenticated = true;
                saveToken();
                logger.info("Authentication successful: {}", username);
                return true;
            }

            logger.error("Failed to get Minecraft profile");
            return false;

        } catch (Exception e) {
            logger.error("Authentication failed", e);
            return false;
        }
    }

    private void showDeviceCodeDialog(String userCode, String verificationUri, AtomicBoolean dialogClosed, Stage[] dialogRef) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("Microsoft Login");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            dialogRef[0] = stage;

            Label titleLabel = new Label("Sign in with Microsoft");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            titleLabel.setStyle("-fx-text-fill: white;");

            Label instructionLabel = new Label("1. Click \"Open Browser\" below\n2. Enter this code on the page:");
            instructionLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13;");
            instructionLabel.setWrapText(true);
            instructionLabel.setMaxWidth(320);

            Label codeLabel = new Label(userCode);
            codeLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
            codeLabel.setStyle("-fx-text-fill: #4fc3f7; -fx-background-color: #1a1a2e; -fx-padding: 12 24; -fx-background-radius: 8;");

            Label urlLabel = new Label(verificationUri);
            urlLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

            Button openBrowserBtn = new Button("Open Browser");
            openBrowserBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
            openBrowserBtn.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(URI.create(verificationUri));
                } catch (Exception ex) {
                    logger.warn("Failed to open browser", ex);
                }
            });

            Button copyBtn = new Button("Copy Code");
            copyBtn.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
            copyBtn.setOnAction(e -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(userCode);
                Clipboard.getSystemClipboard().setContent(content);
                copyBtn.setText("Copied!");
            });

            javafx.scene.layout.HBox buttonRow = new javafx.scene.layout.HBox(12, openBrowserBtn, copyBtn);
            buttonRow.setAlignment(Pos.CENTER);

            ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(28, 28);
            progress.setStyle("-fx-progress-color: #4fc3f7;");

            Label waitingLabel = new Label("Waiting for login...");
            waitingLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

            javafx.scene.layout.HBox waitingRow = new javafx.scene.layout.HBox(8, progress, waitingLabel);
            waitingRow.setAlignment(Pos.CENTER);

            VBox root = new VBox(16, titleLabel, instructionLabel, codeLabel, urlLabel, buttonRow, waitingRow);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(28, 36, 28, 36));
            root.setStyle("-fx-background-color: #0f0f1a;");
            root.setMaxWidth(400);

            Scene scene = new Scene(root, 420, 340);
            stage.setScene(scene);
            stage.setOnHidden(e -> dialogClosed.set(true));
            stage.show();
        });

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}
    }

    private JsonObject requestDeviceCode() {
        try {
            String body = "client_id=" + CLIENT_ID + "&scope=" + encodeURL(SCOPE) + "&response_type=device_code";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEVICE_CODE_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                logger.info("Device code obtained");
                return result;
            } else {
                logger.error("Device code request failed: {} - {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to request device code", e);
            return null;
        }
    }

    private JsonObject pollForToken(String deviceCode) {
        try {
            String body = "grant_type=urn:ietf:params:oauth:grant-type:device_code" +
                    "&client_id=" + CLIENT_ID +
                    "&device_code=" + deviceCode;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class);
            }
        } catch (Exception e) {
            logger.debug("Token poll attempt...");
        }
        return null;
    }

    private String getXboxLiveToken(String msToken) {
        try {
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "t=" + msToken);

            JsonObject bodyObj = new JsonObject();
            bodyObj.add("Properties", properties);
            bodyObj.addProperty("RelyingParty", "http://auth.xboxlive.com");
            bodyObj.addProperty("TokenType", "JWT");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(XBOX_AUTH_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObj)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class).get("Token").getAsString();
            } else {
                logger.error("XBL auth failed: {}", response.body());
            }
        } catch (Exception e) {
            logger.error("XBL auth exception", e);
        }
        return null;
    }

    private JsonObject getXSTSToken(String xblToken) {
        try {
            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            com.google.gson.JsonArray userTokens = new com.google.gson.JsonArray();
            userTokens.add(xblToken);
            properties.add("UserTokens", userTokens);

            JsonObject bodyObj = new JsonObject();
            bodyObj.add("Properties", properties);
            bodyObj.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            bodyObj.addProperty("TokenType", "JWT");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(XBOX_XSTS_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObj)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class);
            } else {
                logger.error("XSTS auth failed: {}", response.body());
            }
        } catch (Exception e) {
            logger.error("XSTS auth exception", e);
        }
        return null;
    }

    private String getMinecraftToken(String xstsToken, String userHash) {
        try {
            JsonObject bodyObj = new JsonObject();
            bodyObj.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MINECRAFT_AUTH_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObj)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class).get("access_token").getAsString();
            } else {
                logger.error("Minecraft token auth failed: {}", response.body());
            }
        } catch (Exception e) {
            logger.error("Minecraft token auth exception", e);
        }
        return null;
    }

    private boolean getMinecraftProfile() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MINECRAFT_PROFILE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject profile = gson.fromJson(response.body(), JsonObject.class);
                username = profile.get("name").getAsString();
                uuid = profile.get("id").getAsString();
                logger.info("Got Minecraft profile: {}", username);
                return true;
            } else {
                logger.error("Failed to get profile: {}", response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to get Minecraft profile", e);
            return false;
        }
    }

    private void saveToken() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            tokenFile.getParentFile().mkdirs();

            JsonObject tokenData = new JsonObject();
            tokenData.addProperty("access_token", accessToken);
            tokenData.addProperty("refresh_token", refreshToken);
            tokenData.addProperty("expires_at", expiresAt);
            tokenData.addProperty("username", username);
            tokenData.addProperty("uuid", uuid);

            try (FileWriter writer = new FileWriter(tokenFile)) {
                writer.write(gson.toJson(tokenData));
            }

            logger.info("Token saved");
        } catch (Exception e) {
            logger.error("Failed to save token", e);
        }
    }

    private void loadCachedToken() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            if (!tokenFile.exists()) {
                logger.debug("No cached token");
                return;
            }

            try (FileReader reader = new FileReader(tokenFile)) {
                JsonObject tokenData = gson.fromJson(reader, JsonObject.class);

                accessToken = tokenData.get("access_token").getAsString();
                username = tokenData.get("username").getAsString();
                uuid = tokenData.get("uuid").getAsString();
                expiresAt = tokenData.get("expires_at").getAsLong();

                if (System.currentTimeMillis() > expiresAt) {
                    logger.info("Token expired");
                    accessToken = null;
                } else {
                    authenticated = true;
                    logger.info("Loaded cached token for: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load token", e);
        }
    }

    public boolean isAuthenticated() {
        return authenticated && accessToken != null && System.currentTimeMillis() < expiresAt;
    }

    public String getUsername() {
        return username;
    }

    public String getUUID() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void logout() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            if (tokenFile.exists()) {
                tokenFile.delete();
            }
            accessToken = null;
            username = null;
            uuid = null;
            authenticated = false;
            logger.info("Logged out");
        } catch (Exception e) {
            logger.error("Failed to logout", e);
        }
    }

    private String encodeURL(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return str;
        }
    }
}
