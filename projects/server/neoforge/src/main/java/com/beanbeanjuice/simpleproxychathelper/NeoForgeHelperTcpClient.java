package com.beanbeanjuice.simpleproxychathelper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Same TCP Client mirrored from the Fabric version.
 */
public class NeoForgeHelperTcpClient {

    private static final long INITIAL_RECONNECT_DELAY_MS = 2_000;
    private static final long MAX_RECONNECT_DELAY_MS = 60_000;

    private final String host;
    private final int port;
    private final String secret;
    private final String serverName;
    private final Logger logger;

    private final ExecutorService sender = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SPC-NeoForgeHelper-Send");
        t.setDaemon(true);
        return t;
    });

    private volatile PrintWriter writer;
    private volatile boolean running = true;
    private volatile Socket socket;

    public NeoForgeHelperTcpClient(String host, int port, String secret, String serverName, Logger logger) {
        this.host = host;
        this.port = port;
        this.secret = secret;
        this.serverName = serverName;
        this.logger = logger;
    }

    public void start() {
        Thread connectThread = new Thread(this::connectLoop, "SPC-NeoForgeHelper-Connect");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    public void stop() {
        running = false;
        sender.shutdownNow();
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
        String line = json.toString();

        sender.execute(() -> {
            synchronized (w) {
                w.println(line);
            }
        });
    }

    private void connectLoop() {
        long reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;

        while (running) {
            try {
                socket = new Socket(host, port);
                PrintWriter w = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                JsonObject auth = new JsonObject();
                auth.addProperty("type", "AUTH");
                auth.addProperty("serverName", serverName);
                auth.addProperty("secret", secret);
                auth.addProperty("papiEnabled", false);
                w.println(auth);

                String ackLine = r.readLine();
                if (ackLine == null) throw new IOException("Connection closed before AUTH_ACK.");

                JsonObject ack = JsonParser.parseString(ackLine).getAsJsonObject();
                if (!ack.get("success").getAsBoolean()) {
                    logger.error("SimpleProxyChatHelper: authentication rejected by proxy: {}",
                            ack.has("reason") ? ack.get("reason").getAsString() : "unknown reason");
                    running = false;
                    return;
                }

                logger.info("SimpleProxyChatHelper: connected to proxy TCP server as '{}'.", serverName);
                writer = w;
                reconnectDelayMs = INITIAL_RECONNECT_DELAY_MS;

                // Drain messages from papi.
                while (r.readLine() != null) { }

                writer = null;
                socket.close();
            } catch (IOException e) {
                writer = null;
                if (!running) return;
                logger.warn("SimpleProxyChatHelper: lost connection to proxy TCP server. Reconnecting in {}s... ({})",
                        reconnectDelayMs / 1000, e.getMessage());
            }

            try {
                Thread.sleep(reconnectDelayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
            reconnectDelayMs = Math.min(reconnectDelayMs * 2, MAX_RECONNECT_DELAY_MS);
        }
    }

}
