package com.beanbeanjuice.proxychat.chat;

import com.beanbeanjuice.proxychat.ProxyChatVelocity;
import com.beanbeanjuice.proxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class VelocityServerListener {

    private ProxyChatVelocity plugin;
    private ChatHandler chatHandler;

    public VelocityServerListener(ProxyChatVelocity plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        ServerConnection serverConnection = event.getPlayer().getCurrentServer().get();
        String serverName = serverConnection.getServerInfo().getName();
        String playerName = event.getPlayer().getUsername();
        String playerMessage = event.getMessage();

        chatHandler.runProxyChatMessage(serverName, playerName, playerMessage, plugin.getLogger()::info, (message) -> {
            List<UUID> blacklistedUUIDs = serverConnection.getServer().getPlayersConnected().stream()
                    .map(Player::getUniqueId)
                    .toList();

            Component component = MiniMessage.miniMessage().deserialize(message);

            plugin.getProxyServer().getAllPlayers().stream()
                    .filter((streamPlayer) -> !blacklistedUUIDs.contains(streamPlayer.getUniqueId()))
                    .forEach((streamPlayer) -> streamPlayer.sendMessage(component));
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
