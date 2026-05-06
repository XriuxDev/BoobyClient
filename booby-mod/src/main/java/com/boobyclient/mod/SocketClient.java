package com.boobyclient.mod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket client for mod ↔ launcher communication
 * Connects to launcher socket server on localhost:25555
 */
public class SocketClient {
    private static final Logger logger = LoggerFactory.getLogger("booby-socket");
    private static final String HOST = "localhost";
    private static final int PORT = 25555;
    private static final Gson gson = new Gson();

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ExecutorService threadPool;
    private boolean connected = false;

    public SocketClient() {
        this.threadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * Connect to launcher socket server
     */
    public void connect() {
        threadPool.execute(() -> {
            try {
                logger.info("Attempting to connect to launcher on {}:{}", HOST, PORT);
                socket = new Socket(HOST, PORT);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;

                logger.info("Connected to launcher socket server");

                // Send initial connection message
                sendMessage("game_running", null);

                // Listen for incoming messages
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    handleMessage(line);
                }

            } catch (IOException e) {
                logger.warn("Failed to connect to launcher: {}", e.getMessage());
                connected = false;
            } catch (Exception e) {
                logger.error("Socket client error", e);
                connected = false;
            } finally {
                disconnect();
            }
        });
    }

    /**
     * Handle incoming message from launcher
     */
    private void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            logger.debug("Received from launcher: type={}", type);

            switch (type) {
                case "toggle_mod":
                    if (json.has("data")) {
                        JsonObject data = json.getAsJsonObject("data");
                        String module = data.get("module").getAsString();
                        boolean enabled = data.get("enabled").getAsBoolean();
                        handleToggleMod(module, enabled);
                    }
                    break;
                default:
                    logger.debug("Unknown message type from launcher: {}", type);
            }
        } catch (Exception e) {
            logger.error("Failed to handle launcher message", e);
        }
    }

    /**
     * Handle module toggle command from launcher
     */
    private void handleToggleMod(String module, boolean enabled) {
        if (BoobyMod.hudManager != null) {
            BoobyMod.hudManager.toggleModule(module, enabled);
            logger.info("Toggled module {} to {}", module, enabled);
        }
    }

    /**
     * Send message to launcher
     */
    public void sendMessage(String type, JsonObject data) {
        if (!connected || writer == null) {
            return;
        }

        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", type);
            if (data != null) {
                message.add("data", data);
            }

            writer.println(gson.toJson(message));
            logger.debug("Sent to launcher: type={}", type);
        } catch (Exception e) {
            logger.error("Failed to send message to launcher", e);
        }
    }

    /**
     * Report module state to launcher
     */
    public void reportModuleState(String module, boolean enabled) {
        JsonObject data = new JsonObject();
        data.addProperty("module", module);
        data.addProperty("enabled", enabled);
        sendMessage("mod_state", data);
    }

    /**
     * Check if connected to launcher
     */
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected();
    }

    /**
     * Disconnect from launcher
     */
    public void disconnect() {
        try {
            connected = false;
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            logger.info("Disconnected from launcher");
        } catch (IOException e) {
            logger.error("Error closing socket connection", e);
        }
    }

    /**
     * Shutdown the client
     */
    public void shutdown() {
        disconnect();
        threadPool.shutdown();
    }
}
