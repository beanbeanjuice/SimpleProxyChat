package com.beanbeanjuice.simpleproxychathelper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;

public class HelperTcpClient {

    private static final long INITIAL_RECONNECT_DELAY_MS = 2_000;
    private static final long MAX_RECONNECT_DELAY_MS = 60_000;

    private final String host;
    private final int port;
    private final String secret;
    private final String serverName;
    private final boolean papiEnabled;
    private final JavaPlugin plugin;
    private final Logger logger;

    private volatile PrintWriter writer;
    private volatile boolean running = true;
    private volatile Socket socket;

    public HelperTcpClient(String host, int port, String secret, String serverName,
                           boolean papiEnabled, JavaPlugin plugin) {
        this.host = host;
        this.port = port;
        this.secret = secret;
        this.serverName = serverName;
        this.papiEnabled = papiEnabled;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void start() {
        Thread connectThread = new Thread(this::connectLoop, "SPC-Helper-Connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public void sendChat(String playerName, UUID playerUUID, String rawMessage) {
        PrintWriter w = writer;
        if (w == null) return;

        JsonObject json = new JsonObject();
        json.addProperty("type", "CHAT");
        json.addProperty("playerName", playerName);
        json.addProperty("playerUUID", playerUUID.toString());
        json.addProperty("rawMessage", rawMessage);

        synchronized (w) {
            w.println(json);
        }
    }

    private void connectLoop() {
        long reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;

        while (running) {
            try {
                socket = new Socket(host, port);
                PrintWriter w = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authenticate
                JsonObject auth = new JsonObject();
                auth.addProperty("type", "AUTH");
                auth.addProperty("serverName", serverName);
                auth.addProperty("secret", secret);
                auth.addProperty("papiEnabled", papiEnabled);
                w.println(auth);

                String ackLine = r.readLine();
                if (ackLine == null) throw new IOException("Connection closed before AUTH_ACK.");

                JsonObject ack = JsonParser.parseString(ackLine).getAsJsonObject();
                if (!ack.get("success").getAsBoolean()) {
                    logger.severe("Helper authentication rejected by proxy: "
                            + (ack.has("reason") ? ack.get("reason").getAsString() : "unknown reason"));
                    running = false;
                    return;
                }

                logger.info("Connected to proxy TCP server as '" + serverName + "'.");
                writer = w;
                reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;

                // Read loop
                String line;
                while ((line = r.readLine()) != null) {
                    handleMessage(JsonParser.parseString(line).getAsJsonObject(), w);
                }

                writer = null;
                socket.close();
            } catch (IOException e) {
                writer = null;
                if (!running) return;
                logger.warning("Lost connection to proxy TCP server. Reconnecting in "
                        + (reconnectDelayMs / 1000) + "s... (" + e.getMessage() + ")");
            }

            try { Thread.sleep(reconnectDelayMs); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
            reconnectDelayMs = Math.min(reconnectDelayMs * 2, MAX_RECONNECT_DELAY_MS);
        }
    }

    private void handleMessage(JsonObject json, PrintWriter w) {
        String type = json.get("type").getAsString();
        if (!"CHAT_FORMAT".equals(type)) return;

        String requestId = json.get("requestId").getAsString();
        String playerName = json.get("playerName").getAsString();
        String minecraft = json.get("minecraft").getAsString();
        String discord = json.get("discord").getAsString();
        String discordEmbedTitle = json.get("discordEmbedTitle").getAsString();
        String discordEmbedMessage = json.get("discordEmbedMessage").getAsString();

        if (papiEnabled) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                minecraft = PlaceholderAPI.setPlaceholders(player, minecraft);
                discord = PlaceholderAPI.setPlaceholders(player, discord);
                discordEmbedTitle = PlaceholderAPI.setPlaceholders(player, discordEmbedTitle);
                discordEmbedMessage = PlaceholderAPI.setPlaceholders(player, discordEmbedMessage);
            }
        }

        JsonObject response = new JsonObject();
        response.addProperty("type", "CHAT_RESOLVED");
        response.addProperty("requestId", requestId);
        response.addProperty("minecraft", minecraft);
        response.addProperty("discord", discord);
        response.addProperty("discordEmbedTitle", discordEmbedTitle);
        response.addProperty("discordEmbedMessage", discordEmbedMessage);

        synchronized (w) {
            w.println(response);
        }
    }

}
