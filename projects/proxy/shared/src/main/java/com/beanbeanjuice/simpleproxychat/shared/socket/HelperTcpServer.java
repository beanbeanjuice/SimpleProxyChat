package com.beanbeanjuice.simpleproxychat.shared.socket;

import com.beanbeanjuice.simpleproxychat.shared.chat.ChatHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HelperTcpServer {

    public HelperTcpServer(String secret, ChatHandler chatHandler, ChatMessageDataFactory dataFactory, Consumer<String> logger) {
        this.secret = secret;
        this.chatHandler = chatHandler;
        this.dataFactory = dataFactory;
        this.logger = logger;
    }

    @FunctionalInterface
    public interface ChatMessageDataFactory {
        Optional<ChatMessageData> create(String playerName, UUID playerUUID, String serverName, String rawMessage);
    }

    @RequiredArgsConstructor
    private static class PendingRequest {
        final ChatMessageData data;
        final long expiresAt;
    }

    private ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Map<String, PrintWriter> connectedHelpers = new ConcurrentHashMap<>();
    private final Map<String, Boolean> helperPapiEnabled = new ConcurrentHashMap<>();
    private final Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private final String secret;
    private final ChatHandler chatHandler;
    private final ChatMessageDataFactory dataFactory;
    private final Consumer<String> logger;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        Thread acceptThread = new Thread(this::acceptLoop, "SPC-Helper-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        logger.accept("Helper TCP server started on port " + port + ".");
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        executor.shutdownNow();
    }

    public boolean hasConnectedHelper(String serverName) {
        return connectedHelpers.containsKey(serverName);
    }

    public boolean isPapiEnabled(String serverName) {
        return helperPapiEnabled.getOrDefault(serverName, false);
    }

    public void sendChatFormat(ChatMessageData data, String minecraft, String discord,
                               String discordEmbedTitle, String discordEmbedMessage) {
        PrintWriter writer = connectedHelpers.get(data.getServername());
        if (writer == null) return;

        String requestId = UUID.randomUUID().toString();
        pendingRequests.put(requestId, new PendingRequest(data, System.currentTimeMillis() + 5_000));

        JsonObject json = new JsonObject();
        json.addProperty("type", "CHAT_FORMAT");
        json.addProperty("requestId", requestId);
        json.addProperty("playerName", data.getPlayerName());
        json.addProperty("rawMessage", data.getMessage());
        json.addProperty("minecraft", minecraft);
        json.addProperty("discord", discord);
        json.addProperty("discordEmbedTitle", discordEmbedTitle);
        json.addProperty("discordEmbedMessage", discordEmbedMessage);

        synchronized (writer) {
            writer.println(json);
        }
    }

    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Socket client = serverSocket.accept();
                executor.submit(() -> handleConnection(client));
            } catch (IOException e) {
                if (!serverSocket.isClosed())
                    logger.accept("Error accepting helper connection: " + e.getMessage());
            }
        }
    }

    private void handleConnection(Socket client) {
        String serverName = null;
        PrintWriter writer = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                String type = json.get("type").getAsString();

                switch (type) {
                    case "AUTH" -> {
                        serverName = handleAuth(json, writer);
                        if (serverName != null) logger.accept("Helper connected: " + serverName);
                    }
                    case "CHAT" -> {
                        if (serverName != null) handleChat(json, serverName);
                    }
                    case "CHAT_RESOLVED" -> handleChatResolved(json);
                }
            }
        } catch (IOException e) {
            // Connection closed or reset — handled in finally
        } finally {
            if (serverName != null) {
                connectedHelpers.remove(serverName);
                helperPapiEnabled.remove(serverName);
                logger.accept("Helper disconnected: " + serverName);
            }
            if (writer != null) writer.close();
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private String handleAuth(JsonObject json, PrintWriter writer) {
        String clientSecret = json.get("secret").getAsString();
        String clientServerName = json.get("serverName").getAsString();
        boolean papiEnabled = json.has("papiEnabled") && json.get("papiEnabled").getAsBoolean();

        JsonObject ack = new JsonObject();
        ack.addProperty("type", "AUTH_ACK");

        if (!clientSecret.equals(secret)) {
            ack.addProperty("success", false);
            ack.addProperty("reason", "Invalid secret.");
            writer.println(ack);
            return null;
        }

        connectedHelpers.put(clientServerName, writer);
        helperPapiEnabled.put(clientServerName, papiEnabled);
        ack.addProperty("success", true);
        writer.println(ack);
        return clientServerName;
    }

    private void handleChat(JsonObject json, String serverName) {
        String playerName = json.get("playerName").getAsString();
        UUID playerUUID = UUID.fromString(json.get("playerUUID").getAsString());
        String rawMessage = json.get("rawMessage").getAsString();

        dataFactory.create(playerName, playerUUID, serverName, rawMessage)
                .ifPresent(chatHandler::runProxyChatMessage);
    }

    private void handleChatResolved(JsonObject json) {
        String requestId = json.get("requestId").getAsString();
        PendingRequest pending = pendingRequests.remove(requestId);
        if (pending == null || System.currentTimeMillis() > pending.expiresAt) return;

        chatHandler.chat(
                pending.data,
                json.get("minecraft").getAsString(),
                json.get("discord").getAsString(),
                json.get("discordEmbedTitle").getAsString(),
                json.get("discordEmbedMessage").getAsString()
        );
    }

}
