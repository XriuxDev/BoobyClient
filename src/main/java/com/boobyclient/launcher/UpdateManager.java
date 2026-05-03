package com.boobyclient.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Locale;

public class UpdateManager {
    private static final Logger logger = LoggerFactory.getLogger(UpdateManager.class);

    private static final String CURRENT_VERSION = "1.0.0";
    private static final String UPDATE_BASE_URL = "https://xriuxdev.github.io/BoobyClient/";
    private static final String UPDATE_JSON_URL = UPDATE_BASE_URL + "update.json";

    private static final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    private static final Gson gson = new Gson();

    public static class UpdateResult {
        public boolean updateLaunched;
        public boolean updateRequiredFailed;
        public String errorMessage;
    }

    public static UpdateResult checkForUpdates() {
        UpdateResult result = new UpdateResult();

        try {
            JsonObject updateInfo = fetchUpdateInfo();
            if (updateInfo == null) {
                result.errorMessage = "Failed to fetch update manifest";
                return result;
            }

            String latestVersion = getString(updateInfo, "version");
            String installerUrl = getString(updateInfo, "installerUrl");
            String sha256 = getString(updateInfo, "sha256");

            if (latestVersion == null || installerUrl == null) {
                result.errorMessage = "Update manifest missing version or installerUrl";
                return result;
            }

            if (compareVersions(latestVersion, CURRENT_VERSION) <= 0) {
                logger.info("No update available. Current: {}, Latest: {}", CURRENT_VERSION, latestVersion);
                return result;
            }

            logger.info("Update available: {} -> {}", CURRENT_VERSION, latestVersion);
            Path installerPath = downloadInstaller(installerUrl);
            if (installerPath == null) {
                result.updateRequiredFailed = true;
                result.errorMessage = "Failed to download update installer";
                return result;
            }

            if (sha256 != null && !sha256.isEmpty()) {
                String actual = sha256(installerPath);
                if (!sha256.equalsIgnoreCase(actual)) {
                    result.updateRequiredFailed = true;
                    result.errorMessage = "Update hash mismatch";
                    return result;
                }
            }

            if (!launchInstaller(installerPath)) {
                result.updateRequiredFailed = true;
                result.errorMessage = "Failed to launch installer";
                return result;
            }

            result.updateLaunched = true;
            return result;

        } catch (Exception e) {
            result.errorMessage = e.getMessage();
            return result;
        }
    }

    private static JsonObject fetchUpdateInfo() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(UPDATE_JSON_URL))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            logger.error("Update manifest request failed: {}", response.statusCode());
            return null;
        }

        return gson.fromJson(response.body(), JsonObject.class);
    }

    private static Path downloadInstaller(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            logger.error("Installer download failed: {}", response.statusCode());
            return null;
        }

        Path tempFile = Files.createTempFile("boobylauncher-update-", ".exe");
        try (InputStream input = response.body()) {
            Files.copy(input, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return tempFile;
    }

    private static boolean launchInstaller(Path installerPath) {
        try {
            String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", installerPath.toString()).start();
                return true;
            }

            logger.error("Unsupported OS for installer: {}", os);
            return false;
        } catch (Exception e) {
            logger.error("Failed to launch installer", e);
            return false;
        }
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private static int compareVersions(String a, String b) {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int max = Math.max(aParts.length, bParts.length);

        for (int i = 0; i < max; i++) {
            int aVal = i < aParts.length ? parseVersionPart(aParts[i]) : 0;
            int bVal = i < bParts.length ? parseVersionPart(bParts[i]) : 0;
            if (aVal != bVal) {
                return Integer.compare(aVal, bVal);
            }
        }

        return 0;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];

        try (InputStream input = Files.newInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
