package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatus;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import de.myzelyam.api.vanish.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeServerListener implements Listener {

    private final SimpleProxyChatBungee plugin;
    private final ChatHandler chatHandler;

    public BungeeServerListener(SimpleProxyChatBungee plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;

        startServerStatusDetection();
    }

    @EventHandler
    public void onProxyChatEvent(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) return;  // Ignore if it is a command.

        Server currentServer = (Server) event.getReceiver();
        String serverName = currentServer.getInfo().getName().toUpperCase();
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String playerName = player.getName();
        String playerMessage = event.getMessage();

        chatHandler.runProxyChatMessage(serverName, playerName, playerMessage, plugin.getLogger()::info, (message) -> {
            List<UUID> blacklistedUUIDs = currentServer.getInfo().getPlayers().stream()
                    .map(ProxiedPlayer::getUniqueId)
                    .toList();

            plugin.getProxy().getPlayers().stream()
                    .filter((streamPlayer) -> !blacklistedUUIDs.contains(streamPlayer.getUniqueId()))
                    .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(message)));
        });
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer());
    }

    void leave(ProxiedPlayer player) {
        chatHandler.runProxyLeaveMessage(player.getName(), player.getUniqueId(), plugin.getLogger()::info, this::sendToAllServers);
    }

    @EventHandler
    public void onPlayerJoinProxy(PostLoginEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        join(event.getPlayer());
    }

    void join(ProxiedPlayer player) {
        chatHandler.runProxyJoinMessage(player.getName(), player.getUniqueId(), plugin.getLogger()::info, this::sendToAllServers);
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(player)) return;  // Ignore if player is invisible.
        if (event.getFrom() == null) return;  // This means the player just joined the network.

        ServerInfo from = event.getFrom();

        chatHandler.runProxySwitchMessage(
                event.getFrom().getName(),
                event.getPlayer().getServer().getInfo().getName(),
                player.getName(),
                player.getUniqueId(),
                plugin.getLogger()::info,
                (message) -> from.getPlayers().stream()
                        .filter((streamPlayer) -> streamPlayer != player)
                        .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(message)))
        );
    }

    private void startServerStatusDetection() {
        ServerStatusManager manager = new ServerStatusManager(plugin.getConfig());
        int updateInterval = (int) plugin.getConfig().get(ConfigDataKey.SERVER_UPDATE_INTERVAL);

        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getProxy().getServers().forEach((serverName, serverInfo) -> {
            serverInfo.ping((result, error) -> {
                boolean newStatus = (error == null);  // Server offline if error != null
                runStatusLogic(manager, serverName, newStatus);
            });
        }), updateInterval, updateInterval, TimeUnit.SECONDS);
    }

    private void runStatusLogic(ServerStatusManager manager, String serverName, boolean newStatus) {
        ServerStatus currentStatus = manager.getStatus(serverName);
        currentStatus.updateStatus(newStatus).ifPresent((isOnline) -> {
            plugin.getDiscordBot().sendMessageEmbed(manager.getStatusEmbed(serverName, isOnline));
            plugin.getLogger().info(manager.getStatusString(serverName, isOnline));
        });
    }

    private void sendToAllServers(String message) {
        plugin.getProxy().broadcast(convertToBungee(message));
    }

    private BaseComponent[] convertToBungee(String message) {
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }

}
