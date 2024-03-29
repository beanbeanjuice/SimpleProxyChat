package com.beanbeanjuice.simpleproxychat.utility.listeners.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatus;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import litebans.api.Database;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityServerListener {

    @Getter private ServerStatusManager serverStatusManager;
    private final SimpleProxyChatVelocity plugin;
    private final ChatHandler chatHandler;

    public VelocityServerListener(SimpleProxyChatVelocity plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
        startServerStatusDetection();
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.LITEBANS_ENABLED) && Database.get().isPlayerMuted(event.getPlayer().getUniqueId(), null)) return;

        event.getPlayer().getCurrentServer().ifPresent((connection) -> {
            String serverName = connection.getServerInfo().getName();
            String playerName = event.getPlayer().getUsername();
            String playerMessage = event.getMessage();

            chatHandler.runProxyChatMessage(serverName, playerName, event.getPlayer().getUniqueId(), playerMessage,
                    (message) -> {
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
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer());
    }

    private void leave(Player player) {
        String serverName = "no-server";
        if (player.getCurrentServer().isPresent())
            serverName = player.getCurrentServer().get().getServerInfo().getName();
        chatHandler.runProxyLeaveMessage(player.getUsername(), player.getUniqueId(), serverName, this::sendToAllServers);
    }

    // TODO: Add Vanish API
    private void join(Player player, String serverName) {
        chatHandler.runProxyJoinMessage(player.getUsername(), player.getUniqueId(), serverName, this::sendToAllServers);
    }

    private void startServerStatusDetection() {
        this.serverStatusManager = new ServerStatusManager(plugin.getConfig());
        int updateInterval = plugin.getConfig().getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL);

        plugin.getProxyServer().getScheduler().buildTask(plugin, () -> plugin.getProxyServer().getAllServers().forEach((registeredServer) -> {
            String serverName = registeredServer.getServerInfo().getName();

            registeredServer.ping().thenAccept((ping) -> {  // Server is online.
                this.serverStatusManager.runStatusLogic(serverName, true, plugin.getDiscordBot(), plugin.getLogger()::info);
            }).exceptionally((exception) -> {  // Server is offline.
                this.serverStatusManager.runStatusLogic(serverName, false, plugin.getDiscordBot(), plugin.getLogger()::info);
                return null;
            });
        })).delay(updateInterval, TimeUnit.SECONDS).repeat(updateInterval, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // First Join
        if (event.getPreviousServer().isEmpty()) {
            join(event.getPlayer(), event.getServer().getServerInfo().getName());
            return;
        }

        // Switch Server
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
                (message) -> {
                    Component component = MiniMessage.miniMessage().deserialize(message);
                    previousServer.getPlayersConnected().stream()
                            .filter((streamPlayer) -> streamPlayer != event.getPlayer())
                            .filter((player) -> {
                                if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                                    return player.hasPermission(Permission.READ_SWITCH_MESSAGE.getPermissionNode());
                                return true;
                            })
                            .forEach((streamPlayer) -> streamPlayer.sendMessage(component));
                }
        );
    }

    private void sendToAllServers(String message, Permission permission) {
        plugin.getProxyServer().getAllPlayers().stream()
                        .filter((player) -> {
                            if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                                return player.hasPermission(permission.getPermissionNode());
                            return true;
                        })
                        .forEach((player) -> player.sendMessage(MiniMessage.miniMessage().deserialize(message)));
    }

}
