package com.plyblox.proxychat.chat;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import de.myzelyam.api.vanish.*;
import net.dv8tion.jda.api.EmbedBuilder;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class ServerChatHandler implements Listener {

    private final ProxyChat plugin;

    private static final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    public ServerChatHandler(@NotNull ProxyChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyChatEvent(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) return;  // Ignore if it is a command.

        String minecraftConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MESSAGE_FORMAT);
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_MESSAGE);
        Server currentServer = (Server) event.getReceiver();
        String serverName = currentServer.getInfo().getName().toUpperCase();
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String playerName = player.getName();
        String playerMessage = event.getMessage();

        String minecraftMessage = minecraftConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        String discordMessage = discordConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, minecraftMessage);

        // Log to Discord
        plugin.getBot().sendMessage(discordMessage);

        // Log to Minecraft
        sendToOtherServers(currentServer, minecraftMessage);
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer());
    }

    @EventHandler
    public void onVanish(BungeePlayerHideEvent event) {
        leave(event.getPlayer());
    }

    private void leave(ProxiedPlayer player) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.LEAVE_FORMAT);
        String playerName = player.getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // Log to Discord
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbedBuilder(player, discordMessage).setColor(Color.RED).build());

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerJoinProxy(PostLoginEvent event) {
        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.
        join(event.getPlayer());
    }

    @EventHandler
    public void onAppear(BungeePlayerShowEvent event) {
        join(event.getPlayer());
    }

    private void join(ProxiedPlayer player) {
        String configString = (String) this.plugin.getConfig().get(ConfigDataKey.JOIN_FORMAT);

        String playerName = player.getName();

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        plugin.getLogger().log(Level.INFO, message);

        // Log to Discord
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbedBuilder(player, discordMessage).setColor(Color.GREEN).build());

        // Log to Minecraft
        sendToAllServers(message);
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if ((Boolean) plugin.getConfig().get(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(player)) return;  // Ignore if player is invisible.
        if (event.getFrom() == null) return;  // This means the player just joined the network.

        ServerInfo from = event.getFrom();

        String consoleConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT);
        String discordConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.MINECRAFT_TO_DISCORD_SWITCH);
        String minecraftConfigString = (String) this.plugin.getConfig().get(ConfigDataKey.SWITCH_FORMAT_NO_FROM);

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
        plugin.getBot().sendMessageEmbed(simpleAuthorEmbedBuilder(player, discordMessage).setColor(Color.YELLOW).build());

        // Log to Minecraft
        sendToSpecificServer(minecraftMessage, event.getFrom(), event.getPlayer());
    }

    private EmbedBuilder simpleAuthorEmbedBuilder(@NotNull ProxiedPlayer player, @NotNull String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message, null, getPlayerHeadURL(player));
        return embedBuilder;
    }

    private void sendToOtherServers(@NotNull Server currentServer, @NotNull String message) {
        ArrayList<UUID> blacklistedUUIDs = (ArrayList<UUID>) currentServer.getInfo().getPlayers().stream()
                        .map(ProxiedPlayer::getUniqueId)
                        .toList();

        plugin.getProxy().getPlayers().stream()
                .filter((player) -> !blacklistedUUIDs.contains(player.getUniqueId()))
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder(message).create()));
    }

    private void sendToSpecificServer(@NotNull String message, @NotNull ServerInfo server, @NotNull ProxiedPlayer blacklistedPlayer) {
        server.getPlayers().stream()
                .filter((player) -> player != blacklistedPlayer)
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, new ComponentBuilder(message).create()));
    }

    private void sendToAllServers(@NotNull String message) {
        plugin.getProxy().broadcast(new ComponentBuilder(message).create());
    }

    private String getPlayerHeadURL(@NotNull ProxiedPlayer player) {
        return MINECRAFT_PLAYER_HEAD_URL.replace("{PLAYER_UUID}", player.getUniqueId().toString());
    }

}
