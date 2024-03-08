package com.beanbeanjuice.simpleproxychat.utility.listeners.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatus;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityServerListener {

    private final SimpleProxyChatVelocity plugin;
    private final ChatHandler chatHandler;

    public VelocityServerListener(SimpleProxyChatVelocity plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
        startServerStatusDetection();
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        event.getPlayer().getCurrentServer().ifPresent((connection) -> {
            String serverName = connection.getServerInfo().getName();
            String playerName = event.getPlayer().getUsername();
            String playerMessage = event.getMessage();

            chatHandler.runProxyChatMessage(serverName, playerName, event.getPlayer().getUniqueId(), playerMessage, plugin.getLogger()::info, (message) -> {
                List<UUID> blacklistedUUIDs = connection.getServer().getPlayersConnected().stream()
                        .map(Player::getUniqueId)
                        .toList();

                Component component = MiniMessage.miniMessage().deserialize(message);

                plugin.getProxyServer().getAllPlayers().stream()
                        .filter((streamPlayer) -> !blacklistedUUIDs.contains(streamPlayer.getUniqueId()))
                        .forEach((streamPlayer) -> streamPlayer.sendMessage(component));
            });
        });
    }

    // TODO: Add Vanish API
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer());
    }

    private void leave(Player player) {
        chatHandler.runProxyLeaveMessage(player.getUsername(), player.getUniqueId(), plugin.getLogger()::info, this::sendToAllServers);
    }

    // TODO: Add Vanish API
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        join(event.getPlayer());
    }

    private void join(Player player) {
        chatHandler.runProxyJoinMessage(player.getUsername(), player.getUniqueId(), plugin.getLogger()::info, this::sendToAllServers);
    }

    private void startServerStatusDetection() {
        ServerStatusManager manager = new ServerStatusManager(plugin.getConfig());
        int updateInterval = (int) plugin.getConfig().get(ConfigDataKey.SERVER_UPDATE_INTERVAL);

        plugin.getProxyServer().getScheduler().buildTask(plugin, () -> plugin.getProxyServer().getAllServers().forEach((registeredServer) -> {
            String serverName = registeredServer.getServerInfo().getName();

            registeredServer.ping().thenAccept((ping) -> {  // Server is online.
                runStatusLogic(manager, serverName, true);
            }).exceptionally((exception) -> {  // Server is offline.
                runStatusLogic(manager, serverName, false);
                return null;
            });
        })).delay(updateInterval, TimeUnit.SECONDS).repeat(updateInterval, TimeUnit.SECONDS).schedule();
    }

    private void runStatusLogic(ServerStatusManager manager, String serverName, boolean newStatus) {
        ServerStatus currentStatus = manager.getStatus(serverName);
        currentStatus.updateStatus(newStatus).ifPresent((isOnline) -> {
            plugin.getDiscordBot().sendMessageEmbed(manager.getStatusEmbed(serverName, isOnline));
            plugin.getLogger().info(manager.getStatusString(serverName, isOnline));
        });
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty()) return;

        RegisteredServer previousServer = event.getPreviousServer().get();
        String from = previousServer.getServerInfo().getName();
        String to = event.getServer().getServerInfo().getName();
        String playerName = event.getPlayer().getUsername();
        UUID playerUUID = event.getPlayer().getUniqueId();

        chatHandler.runProxySwitchMessage(
                from,
                to,
                playerName,
                playerUUID,
                plugin.getLogger()::info,
                (message) -> {
                    Component component = MiniMessage.miniMessage().deserialize(message);
                    previousServer.getPlayersConnected().stream()
                            .filter((streamPlayer) -> streamPlayer != event.getPlayer())
                            .forEach((streamPlayer) -> streamPlayer.sendMessage(component));
                }
        );
    }

    private void sendToAllServers(@NotNull String message) {
        plugin.getProxyServer().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

}
