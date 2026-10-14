package com.beanbeanjuice.simpleproxychathelper;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleProxyChatHelperFabric implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("SimpleProxyChatHelper");

    private FabricHelperTcpClient tcpClient;

    @Override
    public void onInitializeServer() {
        Map<String, String> config = loadConfig();

        String host = config.getOrDefault("proxy-host", "localhost");
        int port = parsePort(config.getOrDefault("proxy-port", "8766"));
        String secret = config.getOrDefault("proxy-secret", "CHANGEME");
        String serverName = config.getOrDefault("server-name", "survival");

        tcpClient = new FabricHelperTcpClient(host, port, secret, serverName, LOGGER);
        tcpClient.start();

        registerChatListener();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (tcpClient != null) tcpClient.stop();
        });

        LOGGER.info("SimpleProxyChatHelper (Fabric) enabled — connecting to {}:{}...", host, port);
    }

    private void registerChatListener() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, boundChatType) -> {
            String rawMessage = message.signedContent();
            if (rawMessage == null || rawMessage.isBlank()) return;

            tcpClient.sendChat(
                    sender.getGameProfile().name(),
                    sender.getUUID(),
                    rawMessage
            );
        });
        LOGGER.info("SimpleProxyChatHelper: chat listener registered");
    }

    private Map<String, String> loadConfig() {
        Path configFile = FabricLoader.getInstance().getConfigDir().resolve("simpleproxychathelper.yml");

        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/helper-config.yml")) {
                if (in != null) Files.copy(in, configFile);
            } catch (IOException e) {
                LOGGER.warn("SimpleProxyChatHelper: could not write default config: {}", e.getMessage());
            }
        }

        Map<String, String> map = new LinkedHashMap<>();
        if (!Files.exists(configFile)) return map;

        try {
            for (String line : Files.readAllLines(configFile)) {
                line = line.trim();
                if (line.startsWith("#") || !line.contains(":")) continue;
                int idx = line.indexOf(':');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim().replaceAll("^\"|\"$", "");
                if (!key.isEmpty() && !value.isEmpty()) map.put(key, value);
            }
        } catch (IOException e) {
            LOGGER.warn("SimpleProxyChatHelper: could not read config: {}", e.getMessage());
        }

        return map;
    }

    private static int parsePort(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("SimpleProxyChatHelper: invalid proxy-port '{}', defaulting to 8766", value);
            return 8766;
        }
    }
}
