package com.boobyclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles game launching and process management
 */
public class GameLauncher {
    private static final Logger logger = LoggerFactory.getLogger(GameLauncher.class);

    public static final String MINECRAFT_DIR = resolveMinecraftDir();

    private static String resolveMinecraftDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            return appData + "/.minecraft";
        }
        return System.getProperty("user.home") + "/.minecraft";
    }
    private static final String JAVA_MEMORY = "-Xmx4G";

    public static class LaunchConfig {
        public String version;
        public String account;
        public String uuid;
        public String authToken;
        public String launcherSocket;
        public String profile;
        public HUDConfig hudModules;
    }

    public static class HUDConfig {
        public boolean toggleSprint;
        public boolean fpsCounter;
        public boolean pingDisplay;
        public boolean comboCounter;

        public HUDConfig(boolean toggleSprint, boolean fpsCounter, boolean pingDisplay, boolean comboCounter) {
            this.toggleSprint = toggleSprint;
            this.fpsCounter = fpsCounter;
            this.pingDisplay = pingDisplay;
            this.comboCounter = comboCounter;
        }
    }

    /**
     * Launch Minecraft with the given configuration
     */
    public void launch(LaunchConfig config) throws Exception {
        logger.info("Preparing to launch Minecraft {}", config.version);

        String javaPath = detectJava();
        if (javaPath == null) {
            throw new RuntimeException("Java not found. Please install Java 17 or higher.");
        }
        logger.info("Using Java at: {}", javaPath);

        File minecraftDir = new File(MINECRAFT_DIR);
        if (!minecraftDir.exists()) {
            logger.info("Creating Minecraft directory at {}", MINECRAFT_DIR);
            minecraftDir.mkdirs();
        }

        VersionMeta meta = parseVersion(config.version);
        String librariesClasspath = meta.classpath;
        String mainClass = meta.mainClass;
        String jarPath = meta.jarPath;
        String assetIndex = meta.assetIndex != null ? meta.assetIndex : config.version;
        String nativesDir = MINECRAFT_DIR + "/versions/" + config.version + "/" + config.version + "-natives";

        if (jarPath == null || !new File(jarPath).exists()) {
            throw new RuntimeException(
                "Minecraft game JAR for " + config.version + " is not installed.\n" +
                "Expected JAR at: " + jarPath
            );
        }

        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add(JAVA_MEMORY);
        jvmArgs.add("-Xms2G");
        jvmArgs.add("-XX:+UseG1GC");
        jvmArgs.add("-XX:MaxGCPauseMillis=200");
        jvmArgs.add("-XX:+UnlockExperimentalVMOptions");
        jvmArgs.add("-XX:G1NewSizePercent=20");
        jvmArgs.add("-XX:G1ReservePercent=20");
        jvmArgs.add("-XX:G1HeapRegionSize=32M");
        jvmArgs.add("-Djava.library.path=" + nativesDir);
        jvmArgs.add("-Dminecraft.launcher.brand=booby-client");
        jvmArgs.add("-Dminecraft.launcher.version=1.0");
        jvmArgs.add("-Dfabric.gameVersion=1.21.11"); // Use 1.21.11 internally for mappings/mods
        jvmArgs.add("-Dfabric.mapping.namespace=intermediary");
        jvmArgs.add("-cp");
        jvmArgs.add(librariesClasspath + File.pathSeparator + jarPath);

        List<String> gameArgs = new ArrayList<>();
        gameArgs.add(mainClass);
        gameArgs.add("--username");
        gameArgs.add(config.account);
        gameArgs.add("--version");
        gameArgs.add(config.version);
        gameArgs.add("--gameDir");
        gameArgs.add(MINECRAFT_DIR);
        gameArgs.add("--assetsDir");
        gameArgs.add(MINECRAFT_DIR + "/assets");
        gameArgs.add("--assetIndex");
        gameArgs.add(assetIndex);
        gameArgs.add("--uuid");
        gameArgs.add(config.uuid != null ? config.uuid : "0");
        gameArgs.add("--accessToken");
        gameArgs.add(config.authToken != null ? config.authToken : "0");
        gameArgs.add("--userType");
        gameArgs.add("msa");
        gameArgs.add("--versionType");
        gameArgs.add("release");

        if (config.launcherSocket != null && !config.launcherSocket.isEmpty()) {
            gameArgs.add("--launcher-socket");
            gameArgs.add(config.launcherSocket);
        }

        if (config.profile != null && !config.profile.isEmpty()) {
            gameArgs.add("--profile");
            gameArgs.add(config.profile);
        }

        if (config.hudModules != null) {
            gameArgs.add("--hud-config");
            gameArgs.add(serializeHUDConfig(config.hudModules));
        }

        File argFile = new File(MINECRAFT_DIR, "booby_launch_args.txt");
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(argFile))) {
            for (String arg : jvmArgs) {
                writer.println(quoteArgFileEntry(arg));
            }
            for (String arg : gameArgs) {
                writer.println(quoteArgFileEntry(arg));
            }
        }

        List<String> command = new ArrayList<>();
        command.add(javaPath);
        command.add("@" + argFile.getAbsolutePath());

        logger.info("Launching Minecraft {} as {} (using argfile)", config.version, config.account);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(MINECRAFT_DIR));
        pb.inheritIO();

        try {
            Process process = pb.start();
            logger.info("Minecraft process started (PID: {})", process.pid());

            int exitCode = process.waitFor();
            logger.info("Minecraft exited with code: {}", exitCode);

        } catch (IOException e) {
            logger.error("Failed to start Minecraft process", e);
            throw new RuntimeException("Failed to launch Minecraft: " + e.getMessage());
        } finally {
            argFile.delete();
        }
    }

    private String quoteArgFileEntry(String arg) {
        if (arg.contains(" ") || arg.contains(";") || arg.contains("=")) {
            return "\"" + arg.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return arg;
    }

    private static class VersionMeta {
        String mainClass = null;
        String classpath = "";
        String jarPath = null;
        String assetIndex = null;
    }

    private VersionMeta parseVersion(String version) {
        VersionMeta meta = new VersionMeta();
        List<String> jars = new ArrayList<>();
        
        parseVersionRecursive(version, jars, meta);
        
        if (meta.mainClass == null) meta.mainClass = "net.minecraft.client.main.Main";
        
        logger.info("Resolved {} libraries from version JSON. MainClass: {}", jars.size(), meta.mainClass);
        meta.classpath = String.join(File.pathSeparator, jars);
        return meta;
    }
    
    private void parseVersionRecursive(String version, List<String> jars, VersionMeta meta) {
        File versionDir = new File(MINECRAFT_DIR + "/versions/" + version);
        File versionJson = new File(versionDir, version + ".json");
        
        // If we haven't found a JAR yet, check if this version has one
        if (meta.jarPath == null) {
            File jarFile = new File(versionDir, version + ".jar");
            if (jarFile.exists()) {
                meta.jarPath = jarFile.getAbsolutePath();
            }
        }

        if (!versionJson.exists()) {
            logger.warn("Version JSON not found for {}", version);
            return;
        }

        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(new String(java.nio.file.Files.readAllBytes(versionJson.toPath()))).getAsJsonObject();
            
            if (root.has("mainClass") && meta.mainClass == null) {
                meta.mainClass = root.get("mainClass").getAsString();
            }

            if (root.has("assetIndex") && meta.assetIndex == null) {
                meta.assetIndex = root.getAsJsonObject("assetIndex").get("id").getAsString();
            }

            if (root.has("inheritsFrom")) {
                parseVersionRecursive(root.get("inheritsFrom").getAsString(), jars, meta);
            }

            if (root.has("libraries")) {
                com.google.gson.JsonArray libraries = root.getAsJsonArray("libraries");
                for (int i = 0; i < libraries.size(); i++) {
                    com.google.gson.JsonObject lib = libraries.get(i).getAsJsonObject();

                    if (lib.has("rules") && !isAllowedByRules(lib.getAsJsonArray("rules"))) {
                        continue;
                    }

                    if (lib.has("downloads")) {
                        com.google.gson.JsonObject downloads = lib.getAsJsonObject("downloads");
                        if (downloads.has("artifact")) {
                            String path = downloads.getAsJsonObject("artifact").get("path").getAsString();
                            File jarFile = new File(MINECRAFT_DIR + "/libraries/" + path);
                            if (jarFile.exists() && !jars.contains(jarFile.getAbsolutePath())) {
                                jars.add(jarFile.getAbsolutePath());
                            }
                        }
                    } else if (lib.has("name")) {
                        // Fabric libraries don't always have "downloads" block in older formats
                        // Actually they usually have it in the modern format, but let's handle name parsing just in case
                        String name = lib.get("name").getAsString();
                        String[] parts = name.split(":");
                        if (parts.length == 3) {
                            String path = parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + ".jar";
                            File jarFile = new File(MINECRAFT_DIR + "/libraries/" + path);
                            if (jarFile.exists() && !jars.contains(jarFile.getAbsolutePath())) {
                                jars.add(jarFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse version JSON for " + version, e);
        }
    }

    private boolean isAllowedByRules(com.google.gson.JsonArray rules) {
        boolean allowed = false;
        for (int i = 0; i < rules.size(); i++) {
            com.google.gson.JsonObject rule = rules.get(i).getAsJsonObject();
            String action = rule.get("action").getAsString();
            if (rule.has("os")) {
                String osName = rule.getAsJsonObject("os").get("name").getAsString();
                boolean matches = osName.equals("windows");
                if (action.equals("allow") && matches) allowed = true;
                if (action.equals("disallow") && matches) return false;
            } else {
                if (action.equals("allow")) allowed = true;
            }
        }
        return allowed;
    }

    private String resolveAssetIndex(String version) {
        File versionJson = new File(MINECRAFT_DIR + "/versions/" + version + "/" + version + ".json");
        if (versionJson.exists()) {
            try {
                String jsonContent = new String(java.nio.file.Files.readAllBytes(versionJson.toPath()));
                com.google.gson.JsonObject root = new com.google.gson.Gson().fromJson(jsonContent, com.google.gson.JsonObject.class);
                if (root.has("assetIndex")) {
                    return root.getAsJsonObject("assetIndex").get("id").getAsString();
                }
                if (root.has("assets")) {
                    return root.get("assets").getAsString();
                }
            } catch (Exception e) {
                logger.warn("Failed to read asset index from version JSON", e);
            }
        }
        return version;
    }


    /**
     * Detect Java installation
     */
    private String detectJava() {
        String[] javaPaths = {
            "C:/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot/bin/java.exe",
            System.getenv("JAVA_HOME"),
            "java",
            "C:/Program Files/Java/jdk-17/bin/java.exe",
            "C:/Program Files (x86)/Java/jdk-17/bin/java.exe"
        };

        for (String path : javaPaths) {
            if (path != null && !path.isEmpty()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(path, "-version");
                    Process p = pb.start();
                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        logger.info("Found Java at: {}", path);
                        return path;
                    }
                } catch (Exception e) {
                    // Continue to next path
                }
            }
        }

        return null;
    }

    /**
     * Serialize HUD configuration for passing to game
     */
    private String serializeHUDConfig(HUDConfig config) {
        return String.format("sprint:%b,fps:%b,ping:%b,combo:%b",
            config.toggleSprint,
            config.fpsCounter,
            config.pingDisplay,
            config.comboCounter);
    }
}
