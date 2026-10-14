package com.beanbeanjuice.simpleproxychathelper;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod(SimpleProxyChatHelperNeoForge.MOD_ID)
public class SimpleProxyChatHelperNeoForge {

    public static final String MOD_ID = "simpleproxychathelper";

    private static final Logger LOGGER = LoggerFactory.getLogger("SimpleProxyChatHelper");

    private NeoForgeHelperTcpClient tcpClient;

    public SimpleProxyChatHelperNeoForge(IEventBus modEventBus) {
        Map<String, String> config = loadConfig();

        String host = config.getOrDefault("proxy-host", "localhost");
        int port = parsePort(config.getOrDefault("proxy-port", "8766"));
        String secret = config.getOrDefault("proxy-secret", "CHANGEME");
        String serverName = config.getOrDefault("server-name", "survival");

        tcpClient = new NeoForgeHelperTcpClient(host, port, secret, serverName, LOGGER);
        tcpClient.start();

        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("SimpleProxyChatHelper (NeoForge) enabled... connecting to {}:{}...", host, port);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        if (tcpClient == null) return;
        String raw = event.getRawText();
        if (raw == null || raw.isBlank()) return;

        ServerPlayer player = event.getPlayer();
        if (player == null) return;

        tcpClient.sendChat(player.getGameProfile().name(), player.getUUID(), raw);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (tcpClient != null) tcpClient.stop();
    }

    private Map<String, String> loadConfig() {
        Path configFile = FMLPaths.CONFIGDIR.get().resolve("simpleproxychathelper.yml");

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
