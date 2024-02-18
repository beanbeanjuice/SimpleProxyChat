package com.plyblox.proxychat.chat;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ServerChatHandler implements Listener {

    private final ProxyChat plugin;

    public ServerChatHandler(@NotNull ProxyChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyChatEvent(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) return;  // Ignore if it is a command.

        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.MESSAGE_FORMAT);
        Server currentServer = (Server) event.getReceiver();
        String serverName = currentServer.getInfo().getName().toUpperCase();
        String playerName = ((ProxiedPlayer) event.getSender()).getName();
        String playerMessage = event.getMessage();

        String message = configString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // TODO: Log to Discord

        // Log to Minecraft
        sendToOtherServers(currentServer, message);
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.LEAVE_FORMAT);
        String playerName = event.getPlayer().getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // TODO: Log to Discord

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerJoinProxy(PostLoginEvent event) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.JOIN_FORMAT);
        String playerName = event.getPlayer().getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // TODO: Log to Discord

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        ServerInfo from = event.getFrom();
        String configString = (from == null) ? (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT_NO_FROM) : (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT);

        ProxiedPlayer player = event.getPlayer();

        String fromString = (from == null) ? "" : from.getName().toUpperCase();
        String toString = player.getServer().getInfo().getName().toUpperCase();

        String message = configString
                .replace("%from%", fromString)
                .replace("%to%", toString)
                .replace("%player%", player.getName());

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // TODO: Log to Discord

        // Log to Minecraft
        if (from == null) return;
        sendToSpecificServer(message, (Server) event.getFrom(), event.getPlayer());
    }

    private void sendToOtherServers(@NotNull Server currentServer, @NotNull String message) {
        plugin.getProxy().getPlayers().stream()
                .filter((player) -> player.getServer() != currentServer)
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder().append(message).create()));
    }

    private void sendToSpecificServer(@NotNull String message, @NotNull Server server, @NotNull ProxiedPlayer blacklistedPlayer) {
        server.getInfo().getPlayers().stream()
                .filter((player) -> player != blacklistedPlayer)
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder().append(message).create()));
    }

    private void sendToAllServers(@NotNull String message) {
        plugin.getProxy().broadcast(new ComponentBuilder().append(message).create());
    }

}
