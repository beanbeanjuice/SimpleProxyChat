package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import de.myzelyam.api.vanish.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BungeeServerListener implements Listener {

    @Getter private ServerStatusManager serverStatusManager;
    private final SimpleProxyChatBungee plugin;
    private final ChatHandler chatHandler;
    @Getter private final BungeePreviousServerHandler previousServerHandler;

    public BungeeServerListener(SimpleProxyChatBungee plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
        this.previousServerHandler = new BungeePreviousServerHandler();

        startServerStatusDetection();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onProxyChatEvent(ChatEvent event) {
        if (event.isCancelled()) return;
        if (event.isCommand() || event.isProxyCommand()) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(player)) return;
        if (!Helper.playerCanChat(plugin.getConfig(), player.getUniqueId(), player.getName())) return;

        Server currentServer = (Server) event.getReceiver();
        String serverName = currentServer.getInfo().getName();
        String playerName = player.getName();
        String playerMessage = event.getMessage();

        chatHandler.runProxyChatMessage(serverName, playerName, player.getUniqueId(), playerMessage,
                (message) -> {
                    Collection<ProxiedPlayer> blacklistedPlayers = currentServer.getInfo().getPlayers();

                    plugin.getProxy().getPlayers().stream()
                            .filter((streamPlayer) -> !blacklistedPlayers.contains(streamPlayer))
                            .filter((streamPlayer) -> {
                                for (ProxiedPlayer blacklistedPlayer : blacklistedPlayers)
                                    if (blacklistedPlayer.getName().equals(streamPlayer.getName())) return false;
                                return true;
                            })
                            .filter((streamPlayer) -> {
                                if (!plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS)) return true;
                                return streamPlayer.hasPermission(Permission.READ_CHAT_MESSAGE.getPermissionNode());
                            })
                            .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(message)));
                });
    }

    /*
        This is needed because there is no specific event that can get the previous server AND check if the player
        leaves the proxy.
    */
    @EventHandler
    public void onPlayerLeaveServer(ServerDisconnectEvent event) {
        previousServerHandler.put(event.getPlayer().getName(), event.getTarget());
    }

    /*
        This is needed because there is no specific event that can get the previous server AND check if the player
        leaves the proxy.
    */
    @EventHandler
    public void onPlayerKick(ServerKickEvent event) {
        previousServerHandler.put(event.getPlayer().getName(), event.getKickedFrom());
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer(), false);
    }

    void leave(ProxiedPlayer player, boolean isFake) {
        // Bungee is "dumb" and needs to be delayed...
        try {
            plugin.getProxy().getScheduler().schedule(
                    plugin,
                    () -> {
                        previousServerHandler.get(player.getName()).ifPresent((serverInfo) -> {
                            if (isFake) chatHandler.runProxyLeaveMessage(player.getName(), player.getUniqueId(), serverInfo.getName(), this::sendToAllServersVanish);
                            else chatHandler.runProxyLeaveMessage(player.getName(), player.getUniqueId(), serverInfo.getName(), this::sendToAllServers);
                        });
                    },
                    50L, TimeUnit.MILLISECONDS);  // 50ms is 1 tick
        } catch (Exception e) {
            plugin.getLogger().warning("BungeeCord error. This is a bungeecord issue and cannot be fixed: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoinProxy(ServerConnectedEvent event) {
        if (event.getPlayer().getGroups().contains("not-first-join")) return;  // If not first join, don't do anything.
        event.getPlayer().addGroups("not-first-join");

        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        join(event.getPlayer(), event.getServer(), false);
    }

    public void join(ProxiedPlayer player, Server server, boolean isFake) {
        // Bungee is "dumb" and needs to be delayed...
        try {
            plugin.getProxy().getScheduler().schedule(
                    plugin,
                    () -> {
                        previousServerHandler.put(player.getName(), server.getInfo());

                        if (isFake) chatHandler.runProxyJoinMessage(player.getName(), player.getUniqueId(), server.getInfo().getName(), this::sendToAllServersVanish);
                        else chatHandler.runProxyJoinMessage(player.getName(), player.getUniqueId(), server.getInfo().getName(), this::sendToAllServers);
                    },
                    50L, TimeUnit.MILLISECONDS);  // 50ms is 1 tick
        } catch (Exception e) {
            plugin.getLogger().warning("BungeeCord error. This is a bungeecord issue and cannot be fixed: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && BungeeVanishAPI.isInvisible(player)) return;  // Ignore if player is invisible.
        if (event.getFrom() == null) return;  // This means the player just joined the network.

        ServerInfo from = event.getFrom();
        previousServerHandler.put(player.getName(), event.getPlayer().getServer().getInfo());

        chatHandler.runProxySwitchMessage(
                event.getFrom().getName(),
                event.getPlayer().getServer().getInfo().getName(),
                player.getName(),
                player.getUniqueId(),
                (message) -> from.getPlayers().stream()
                        .filter((streamPlayer) -> streamPlayer != player)
                        .filter((streamPlayer) -> {
                            if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                                return streamPlayer.hasPermission(Permission.READ_SWITCH_MESSAGE.getPermissionNode());
                            return true;
                        })
                        .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(message)))
        );
    }

    private void startServerStatusDetection() {
        this.serverStatusManager = new ServerStatusManager(plugin.getConfig());
        int updateInterval = plugin.getConfig().getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL);

        plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getProxy().getServers().forEach((serverName, serverInfo) -> {
            serverInfo.ping((result, error) -> {
                boolean newStatus = (error == null);  // Server offline if error != null
                this.serverStatusManager.runStatusLogic(serverName, newStatus, plugin.getDiscordBot(), plugin.getLogger()::info);
            });
        }), updateInterval, updateInterval, TimeUnit.SECONDS);
    }

    private void sendToAllServers(String message, Permission permission) {
        plugin.getProxy().getPlayers().stream()
                        .filter((player) -> {
                            if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                                return player.hasPermission(permission.getPermissionNode());
                            return true;
                        })
                        .filter((player) -> !Helper.serverHasChatLocked(plugin.getConfig(), player.getServer().getInfo().getName()))
                        .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, convertToBungee(message)));
    }

    private void sendToAllServersVanish(String message, Permission permission) {
        plugin.getProxy().getPlayers().stream()
                .filter((player) -> {
                    if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                        return player.hasPermission(permission.getPermissionNode());
                    return true;
                })
                .filter((player) -> {
                    if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                        return player.hasPermission(Permission.READ_FAKE_MESSAGE.getPermissionNode());
                    return true;
                })
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, convertToBungee(message)));
    }

    private BaseComponent[] convertToBungee(String message) {
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }

}
