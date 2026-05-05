package com.boobyclient.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GameInstaller {
    private static final Logger logger = LoggerFactory.getLogger(GameInstaller.class);
    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Gson gson = new Gson();
    
    private final String gameDir;
    
    public GameInstaller(String gameDir) {
        this.gameDir = gameDir;
    }

    public CompletableFuture<Void> installVersion(String version, Consumer<String> progressCallback) {
        return CompletableFuture.runAsync(() -> {
            try {
                progressCallback.accept("Fetching version manifest...");
                String manifestJson = fetchUrl(MANIFEST_URL);
                JsonObject manifest = gson.fromJson(manifestJson, JsonObject.class);
                
                String versionUrl = null;
                for (JsonElement elem : manifest.getAsJsonArray("versions")) {
                    JsonObject v = elem.getAsJsonObject();
                    if (v.get("id").getAsString().equals(version)) {
                        versionUrl = v.get("url").getAsString();
                        break;
                    }
                }
                
                if (versionUrl == null) {
                    throw new RuntimeException("Version " + version + " not found in Mojang manifest!");
                }
                
                progressCallback.accept("Downloading " + version + " JSON...");
                String versionJson = fetchUrl(versionUrl);
                JsonObject versionData = gson.fromJson(versionJson, JsonObject.class);
                
                // Save version.json
                File versionDir = new File(gameDir, "versions/" + version);
                versionDir.mkdirs();
                Files.writeString(new File(versionDir, version + ".json").toPath(), versionJson);
                
                // Download Client JAR
                progressCallback.accept("Downloading client.jar...");
                JsonObject downloads = versionData.getAsJsonObject("downloads");
                if (downloads.has("client")) {
                    String clientUrl = downloads.getAsJsonObject("client").get("url").getAsString();
                    File clientJar = new File(versionDir, version + ".jar");
                    if (!clientJar.exists() || clientJar.length() == 0) {
                        downloadFile(clientUrl, clientJar);
                    }
                }
                
                // Download Libraries
                progressCallback.accept("Downloading libraries...");
                JsonArray libraries = versionData.getAsJsonArray("libraries");
                int downloadedLibs = 0;
                for (JsonElement elem : libraries) {
                    JsonObject lib = elem.getAsJsonObject();
                    if (isAllowedByRules(lib)) {
                        if (lib.has("downloads") && lib.getAsJsonObject("downloads").has("artifact")) {
                            JsonObject artifact = lib.getAsJsonObject("downloads").getAsJsonObject("artifact");
                            String path = artifact.get("path").getAsString();
                            String url = artifact.get("url").getAsString();
                            File libFile = new File(gameDir, "libraries/" + path);
                            if (!libFile.exists() || libFile.length() == 0) {
                                downloadFile(url, libFile);
                                downloadedLibs++;
                            }
                        }
                    }
                }
                progressCallback.accept("Downloaded " + downloadedLibs + " missing libraries.");
                
                // Download Assets
                progressCallback.accept("Fetching asset index...");
                JsonObject assetIndex = versionData.getAsJsonObject("assetIndex");
                String assetId = assetIndex.get("id").getAsString();
                String assetIndexUrl = assetIndex.get("url").getAsString();
                
                File indexesDir = new File(gameDir, "assets/indexes");
                indexesDir.mkdirs();
                File assetIndexFile = new File(indexesDir, assetId + ".json");
                
                String assetIndexJson;
                if (!assetIndexFile.exists()) {
                    assetIndexJson = fetchUrl(assetIndexUrl);
                    Files.writeString(assetIndexFile.toPath(), assetIndexJson);
                } else {
                    assetIndexJson = Files.readString(assetIndexFile.toPath());
                }
                
                progressCallback.accept("Downloading assets...");
                JsonObject assetsData = gson.fromJson(assetIndexJson, JsonObject.class).getAsJsonObject("objects");
                int downloadedAssets = 0;
                int totalAssets = assetsData.size();
                int currentAsset = 0;
                
                File objectsDir = new File(gameDir, "assets/objects");
                objectsDir.mkdirs();
                
                for (String key : assetsData.keySet()) {
                    currentAsset++;
                    if (currentAsset % 100 == 0) {
                        progressCallback.accept(String.format("Downloading assets... (%d/%d)", currentAsset, totalAssets));
                    }
                    
                    JsonObject asset = assetsData.getAsJsonObject(key);
                    String hash = asset.get("hash").getAsString();
                    String subHash = hash.substring(0, 2);
                    File assetFile = new File(objectsDir, subHash + "/" + hash);
                    
                    if (!assetFile.exists() || assetFile.length() == 0) {
                        assetFile.getParentFile().mkdirs();
                        String assetUrl = "https://resources.download.minecraft.net/" + subHash + "/" + hash;
                        downloadFile(assetUrl, assetFile);
                        downloadedAssets++;
                    }
                }
                
                progressCallback.accept("Installation complete! Downloaded " + downloadedAssets + " assets.");
                
                // --- BOOBY CLIENT MOD DEPLOYMENT ---
                progressCallback.accept("Deploying Booby Client mod...");
                File modsDir = new File(gameDir, "mods");
                modsDir.mkdirs();
                File modFile = new File(modsDir, "booby-mod.jar");
                
                // Link to your mod on GitHub (using the actual v1.0.8 tag)
                String modUrl = "https://github.com/XriuxDev/BoobyClient/releases/download/v1.0.8/booby-mod.jar";
                downloadFile(modUrl, modFile);
                progressCallback.accept("Booby Client mod deployed successfully!");
                
            } catch (Exception e) {
                logger.error("Installation failed", e);
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isAllowedByRules(JsonObject lib) {
        if (!lib.has("rules")) return true;
        JsonArray rules = lib.getAsJsonArray("rules");
        boolean allowed = false;
        for (JsonElement elem : rules) {
            JsonObject rule = elem.getAsJsonObject();
            String action = rule.get("action").getAsString();
            if (rule.has("os")) {
                String osName = rule.getAsJsonObject("os").get("name").getAsString();
                if (osName.equals("windows")) {
                    allowed = action.equals("allow");
                }
            } else {
                allowed = action.equals("allow");
            }
        }
        return allowed;
    }

    private String fetchUrl(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private void downloadFile(String url, File dest) throws Exception {
        dest.getParentFile().mkdirs();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        
        try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(dest)) {
            in.transferTo(out);
        }
    }
}
