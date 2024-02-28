package com.beanbeanjuice.simpleproxychat.chat;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class BungeeServerListener implements Listener {

    private final SimpleProxyChatBungee plugin;
    private final ChatHandler chatHandler;

    public BungeeServerListener(SimpleProxyChatBungee plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
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
                (message) -> {
                    from.getPlayers().stream()
                            .filter((streamPlayer) -> streamPlayer != player)
                            .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(message)));

                }
        );
    }

    private void sendToAllServers(@NotNull String message) {
        plugin.getProxy().broadcast(convertToBungee(message));
    }

    private @NotNull BaseComponent @NotNull [] convertToBungee(String message) {
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }

}
