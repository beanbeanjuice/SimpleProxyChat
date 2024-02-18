package com.plyblox.proxychat.chat;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.api.ChatColor;
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

    private final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    public ServerChatHandler(@NotNull ProxyChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyChatEvent(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) return;  // Ignore if it is a command.

        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.MESSAGE_FORMAT);
        Server currentServer = (Server) event.getReceiver();
        String serverName = currentServer.getInfo().getName().toUpperCase();
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String playerName = player.getName();
        String playerMessage = event.getMessage();

        String message = configString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // Log to Discord
        plugin.getBot().sendMessageEmbed(playerMessageEmbed(player, serverName, playerMessage));

        // Log to Minecraft
        sendToOtherServers(currentServer, message);
    }

    private MessageEmbed playerMessageEmbed(ProxiedPlayer player, String serverName, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        String title = String.format("%s (%s)", player.getName(), serverName);
        embedBuilder.setAuthor(title, null, getPlayerHeadURL(player));
        embedBuilder.setDescription(message);

        return embedBuilder.build();
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.LEAVE_FORMAT);
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // Log to Discord
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbed(player, discordMessage));

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerJoinProxy(PostLoginEvent event) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.JOIN_FORMAT);
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // Log to Discord
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbed(player, discordMessage));

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        if (event.getFrom() == null) return;  // This means the player just joined the network.

        String consoleConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT);
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_SWITCH);
        String minecraftConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT_NO_FROM);

        ServerInfo from = event.getFrom();
        ProxiedPlayer player = event.getPlayer();

        String fromString = from.getName().toUpperCase();
        String toString = player.getServer().getInfo().getName().toUpperCase();

        String consoleMessage = consoleConfigString
                .replace("%from%", fromString)
                .replace("%to%", toString)
                .replace("%player%", player.getName());

        String discordMessage = discordConfigString
                .replace("%from%", fromString)
                .replace("%to%", toString)
                .replace("%player%", player.getName());

        String minecraftMessage = minecraftConfigString
                .replace("%from%", fromString)
                .replace("%to%", toString)
                .replace("%player%", player.getName());

        // Log to Console
        plugin.getLogger().log(Level.INFO, consoleMessage);

        // Log to Discord
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbed(player, discordMessage));

        // Log to Minecraft
        sendToSpecificServer(minecraftMessage, event.getFrom(), event.getPlayer());
    }

    private MessageEmbed simpleAuthorEmbed(@NotNull ProxiedPlayer player, @NotNull String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message, null, getPlayerHeadURL(player));
        return embedBuilder.build();
    }

    private void sendToOtherServers(@NotNull Server currentServer, @NotNull String message) {
        plugin.getProxy().getPlayers().stream()
                .filter((player) -> player.getServer() != currentServer)
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder().append(message).create()));
    }

    private void sendToSpecificServer(@NotNull String message, @NotNull ServerInfo server, @NotNull ProxiedPlayer blacklistedPlayer) {
        server.getPlayers().stream()
                .filter((player) -> player != blacklistedPlayer)
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder().append(message).create()));
    }

    private void sendToAllServers(@NotNull String message) {
        plugin.getProxy().broadcast(new ComponentBuilder().append(message).create());
    }

    private String getPlayerHeadURL(@NotNull ProxiedPlayer player) {
        return MINECRAFT_PLAYER_HEAD_URL.replace("{PLAYER_UUID}", player.getUniqueId().toString());
    }

}
