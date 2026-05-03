package com.boobyclient.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket server for launcher ↔ mod communication
 * Listens on localhost:25555 for connections from in-game mod
 */
public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private static final int PORT = 25555;
    private static final Gson gson = new Gson();

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running = false;
    private Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader reader;

    // Callbacks for UI updates
    private Runnable onModConnected;
    private Runnable onModDisconnected;

    public SocketServer() {
        this.threadPool = Executors.newFixedThreadPool(2);
    }

    /**
     * Start the socket server
     */
    public void start() {
        threadPool.execute(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                logger.info("Socket server listening on port {}", PORT);

                while (running) {
                    try {
                        logger.debug("Waiting for mod connection...");
                        Socket socket = serverSocket.accept();
                        handleClientConnection(socket);
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Connection error", e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to start socket server", e);
            }
        });
    }

    /**
     * Handle incoming client connection from mod
     */
    private void handleClientConnection(Socket socket) {
        this.clientSocket = socket;
        logger.info("Mod connected from {}", socket.getInetAddress());

        if (onModConnected != null) {
            onModConnected.run();
        }

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while (running && (line = reader.readLine()) != null) {
                handleMessage(line);
            }

        } catch (IOException e) {
            logger.warn("Client disconnected: {}", e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Handle incoming message from mod
     */
    private void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            logger.debug("Received message from mod: type={}", type);

            switch (type) {
                case "mod_state":
                    // Mod reports its state
                    logger.info("Mod state: {}", json);
                    break;
                case "game_running":
                    logger.info("Game is running");
                    break;
                default:
                    logger.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            logger.error("Failed to handle message", e);
        }
    }

    /**
     * Send message to mod
     */
    public void sendMessage(String type, JsonObject data) {
        if (writer == null || clientSocket == null || !clientSocket.isConnected()) {
            logger.warn("Cannot send message - mod not connected");
            return;
        }

        try {
            JsonObject message = new JsonObject();
            message.addProperty("type", type);
            if (data != null) {
                message.add("data", data);
            }

            writer.println(gson.toJson(message));
            logger.debug("Sent message to mod: type={}", type);
        } catch (Exception e) {
            logger.error("Failed to send message to mod", e);
        }
    }

    /**
     * Send toggle command to mod
     */
    public void toggleModule(String module, boolean enabled) {
        JsonObject data = new JsonObject();
        data.addProperty("module", module);
        data.addProperty("enabled", enabled);
        sendMessage("toggle_mod", data);
    }

    /**
     * Disconnect from client
     */
    private void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }

        clientSocket = null;
        writer = null;
        reader = null;

        logger.info("Mod disconnected");

        if (onModDisconnected != null) {
            onModDisconnected.run();
        }
    }

    /**
     * Check if mod is connected
     */
    public boolean isModConnected() {
        return clientSocket != null && clientSocket.isConnected();
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            disconnect();
            threadPool.shutdown();
            logger.info("Socket server stopped");
        } catch (IOException e) {
            logger.error("Error stopping server", e);
        }
    }

    /**
     * Set callback for when mod connects
     */
    public void setOnModConnected(Runnable callback) {
        this.onModConnected = callback;
    }

    /**
     * Set callback for when mod disconnects
     */
    public void setOnModDisconnected(Runnable callback) {
        this.onModDisconnected = callback;
    }
}
