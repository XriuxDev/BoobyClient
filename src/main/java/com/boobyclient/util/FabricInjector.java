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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FabricInjector {
    private static final Logger logger = LoggerFactory.getLogger(FabricInjector.class);
    private static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final Gson gson = new Gson();
    
    private final String gameDir;
    
    public FabricInjector(String gameDir) {
        this.gameDir = gameDir;
    }

    /**
     * Injects Fabric dynamically for the given version and returns the generated Fabric profile name.
     */
    public CompletableFuture<String> injectFabric(String apiVersion, String actualVersion, Consumer<String> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                progressCallback.accept("Querying Fabric Meta API...");
                
                // Get latest Fabric Loader for this version
                String loaderVersionsJson = fetchUrl("https://meta.fabricmc.net/v2/versions/loader/" + apiVersion);
                JsonArray loaderVersions = gson.fromJson(loaderVersionsJson, JsonArray.class);
                
                if (loaderVersions.size() == 0) {
                    throw new RuntimeException("No Fabric Loader found for Minecraft " + apiVersion);
                }
                
                // Get the stable loader version
                String loaderVersion = null;
                for (JsonElement elem : loaderVersions) {
                    JsonObject obj = elem.getAsJsonObject();
                    if (obj.getAsJsonObject("loader").get("stable").getAsBoolean()) {
                        loaderVersion = obj.getAsJsonObject("loader").get("version").getAsString();
                        break;
                    }
                }
                
                // Fallback to latest unstable if no stable exists
                if (loaderVersion == null) {
                    loaderVersion = loaderVersions.get(0).getAsJsonObject().getAsJsonObject("loader").get("version").getAsString();
                }
                
                String profileName = "fabric-loader-" + loaderVersion + "-" + actualVersion;
                progressCallback.accept("Generating " + profileName + "...");
                
                // Get the profile JSON
                String profileJsonStr = fetchUrl("https://meta.fabricmc.net/v2/versions/loader/" + apiVersion + "/" + loaderVersion + "/profile/json");
                
                // Modify the JSON to inherit from the actual local version
                JsonObject profileJson = gson.fromJson(profileJsonStr, JsonObject.class);
                profileJson.addProperty("inheritsFrom", actualVersion);
                profileJson.addProperty("id", profileName);
                profileJsonStr = gson.toJson(profileJson);
                
                File versionDir = new File(gameDir, "versions/" + profileName);
                versionDir.mkdirs();
                File jsonFile = new File(versionDir, profileName + ".json");
                Files.writeString(jsonFile.toPath(), profileJsonStr);
                
                progressCallback.accept("Downloading Fabric libraries...");
                
                // Parse libraries to download them (re-use the profileJson we already have)
                JsonArray libraries = profileJson.getAsJsonArray("libraries");
                
                int downloaded = 0;
                for (JsonElement elem : libraries) {
                    JsonObject lib = elem.getAsJsonObject();
                    String name = lib.get("name").getAsString();
                    String url = lib.has("url") ? lib.get("url").getAsString() : "https://maven.fabricmc.net/";
                    
                    String[] parts = name.split(":");
                    if (parts.length >= 3) {
                        String group = parts[0].replace('.', '/');
                        String artifact = parts[1];
                        String libVersion = parts[2];
                        String jarName = artifact + "-" + libVersion + ".jar";
                        String path = group + "/" + artifact + "/" + libVersion + "/" + jarName;
                        
                        File libFile = new File(gameDir, "libraries/" + path);
                        if (!libFile.exists() || libFile.length() == 0) {
                            String fullUrl = url;
                            if (!fullUrl.endsWith("/")) fullUrl += "/";
                            fullUrl += path;
                            
                            try {
                                downloadFile(fullUrl, libFile);
                                downloaded++;
                            } catch (Exception e) {
                                logger.warn("Failed to download Fabric library: " + fullUrl, e);
                            }
                        }
                    }
                }
                
                progressCallback.accept("Fabric injection complete. Downloaded " + downloaded + " libraries.");
                return profileName;
                
            } catch (Exception e) {
                logger.error("Fabric injection failed", e);
                throw new RuntimeException(e);
            }
        });
    }

    private String fetchUrl(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch " + url + ": HTTP " + response.statusCode());
        }
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
