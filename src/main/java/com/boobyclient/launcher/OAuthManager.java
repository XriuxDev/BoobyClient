package com.boobyclient.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Handles Microsoft OAuth 2.0 authentication using device code flow
 * This is the simplest OAuth flow for desktop applications
 */
public class OAuthManager {
    private static final Logger logger = LoggerFactory.getLogger(OAuthManager.class);
    private static final String CLIENT_ID = "389b1b32-b5d5-43b2-bf39-fc8162ebf8d8"; // Official Minecraft Launcher Client ID
    private static final String TENANT = "consumers";
    private static final String REDIRECT_URI = "http://localhost:3000";
    private static final String AUTH_TOKEN_FILE = System.getProperty("user.home") + "/.boobyclient/auth_token.json";
    private static final String SCOPE = "XboxLive.signin offline_access";

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    private String accessToken;
    private String refreshToken;
    private long expiresAt;
    private String username;

    public OAuthManager() {
        loadCachedToken();
    }

    /**
     * Authenticate using device code flow
     * Opens browser for user to approve authentication
     */
    public boolean authenticate() {
        logger.info("Starting OAuth device code flow");

        try {
            // Step 1: Request device code
            JsonObject deviceCodeResponse = requestDeviceCode();
            if (deviceCodeResponse == null) {
                logger.error("Failed to get device code");
                return false;
            }

            String deviceCode = deviceCodeResponse.get("device_code").getAsString();
            String userCode = deviceCodeResponse.get("user_code").getAsString();
            String verificationUrl = deviceCodeResponse.get("verification_url").getAsString();
            int interval = deviceCodeResponse.get("interval").getAsInt();
            int expiresIn = deviceCodeResponse.get("expires_in").getAsInt();

            // Step 2: Show user code and open browser
            logger.info("Device code: {}", userCode);
            logger.info("Verification URL: {}", verificationUrl);


            // Step 3: Poll for token
            long startTime = System.currentTimeMillis();
            long timeout = expiresIn * 1000L;

            while (System.currentTimeMillis() - startTime < timeout) {
                Thread.sleep(interval * 1000L);

                JsonObject tokenResponse = pollForToken(deviceCode, interval);
                if (tokenResponse != null && tokenResponse.has("access_token")) {
                    // Token acquired
                    accessToken = tokenResponse.get("access_token").getAsString();
                    if (tokenResponse.has("refresh_token")) {
                        refreshToken = tokenResponse.get("refresh_token").getAsString();
                    }
                    expiresAt = System.currentTimeMillis() + (tokenResponse.get("expires_in").getAsInt() * 1000L);

                    // Get username from Xbox Live
                    if (getXboxUsername()) {
                        saveToken();
                        logger.info("Authentication successful for user: {}", username);
                        return true;
                    }
                }
            }

            logger.warn("Authentication timed out");
            return false;

        } catch (Exception e) {
            logger.error("OAuth authentication failed", e);
            return false;
        }
    }

    /**
     * Request device code from Microsoft
     */
    private JsonObject requestDeviceCode() {
        try {
            String body = String.format(
                "client_id=%s&scope=%s",
                CLIENT_ID,
                encodeURL("XboxLive.signin offline_access")
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/devicecode"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "BoobyClient/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject result = gson.fromJson(response.body(), JsonObject.class);
                logger.info("Device code request successful");
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

    /**
     * URL encode a string
     */
    private String encodeURL(String str) {
        try {
            return java.net.URLEncoder.encode(str, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * Poll for token using device code
     */
    private JsonObject pollForToken(String deviceCode, int interval) {
        try {
            String body = String.format(
                "grant_type=urn:ietf:params:oauth:grant-type:device_code&" +
                "client_id=%s&device_code=%s",
                CLIENT_ID,
                deviceCode
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.microsoftonline.com/" + TENANT + "/oauth2/v2.0/token"))
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

    /**
     * Get Xbox Live username from access token
     */
    private boolean getXboxUsername() {
        try {
            // This would normally query Xbox Live API
            // For now, use a placeholder
            username = "MinecraftPlayer";
            return true;
        } catch (Exception e) {
            logger.error("Failed to get Xbox username", e);
            return false;
        }
    }

    /**
     * Save token to file
     */
    private void saveToken() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            tokenFile.getParentFile().mkdirs();

            JsonObject tokenData = new JsonObject();
            tokenData.addProperty("access_token", accessToken);
            tokenData.addProperty("refresh_token", refreshToken);
            tokenData.addProperty("expires_at", expiresAt);
            tokenData.addProperty("username", username);

            try (FileWriter writer = new FileWriter(tokenFile)) {
                writer.write(gson.toJson(tokenData));
            }

            logger.info("Token saved to {}", AUTH_TOKEN_FILE);
        } catch (Exception e) {
            logger.error("Failed to save token", e);
        }
    }

    /**
     * Load cached token from file
     */
    private void loadCachedToken() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            if (!tokenFile.exists()) {
                logger.debug("No cached token found");
                return;
            }

            try (FileReader reader = new FileReader(tokenFile)) {
                JsonObject tokenData = gson.fromJson(reader, JsonObject.class);

                accessToken = tokenData.get("access_token").getAsString();
                refreshToken = tokenData.get("refresh_token").getAsString();
                expiresAt = tokenData.get("expires_at").getAsLong();
                username = tokenData.get("username").getAsString();

                // Check if token is expired
                if (System.currentTimeMillis() > expiresAt) {
                    logger.info("Token expired, will require re-authentication");
                    accessToken = null;
                } else {
                    logger.info("Loaded cached token for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load cached token", e);
        }
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return accessToken != null && System.currentTimeMillis() < expiresAt;
    }

    /**
     * Get access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Logout
     */
    public void logout() {
        try {
            File tokenFile = new File(AUTH_TOKEN_FILE);
            if (tokenFile.exists()) {
                tokenFile.delete();
            }
            accessToken = null;
            refreshToken = null;
            username = null;
            logger.info("User logged out");
        } catch (Exception e) {
            logger.error("Failed to logout", e);
        }
    }

}
