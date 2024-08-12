package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.socket.bungee.BungeeChatMessageData;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.listeners.MessageType;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import de.myzelyam.api.vanish.*;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class BungeeServerListener implements Listener {

    @Getter private ServerStatusManager serverStatusManager;
    private final SimpleProxyChatBungee plugin;
    @Getter private final ChatHandler chatHandler;
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
        if (plugin.isVanishAPIEnabled() && BungeeVanishAPI.isInvisible(player)) {
            // TODO: If is allowed to speak in vanish then continue.
            if (!event.getMessage().endsWith("/")) {
                String errorMessage = plugin.getConfig().getAsString(ConfigDataKey.MINECRAFT_CHAT_VANISHED_MESSAGE);
                player.sendMessage(ChatMessageType.SYSTEM, Helper.convertToBungee(errorMessage));
                return;
            }
            event.setMessage(event.getMessage().substring(0, event.getMessage().length() - 1));
        }
        if (!Helper.playerCanChat(plugin, player.getUniqueId(), player.getName())) return;

        Server currentServer = (Server) event.getReceiver();
        String playerMessage = event.getMessage();
        BungeeChatMessageData messageData = new BungeeChatMessageData(plugin, MessageType.CHAT, currentServer.getInfo(), player, playerMessage);

        chatHandler.runProxyChatMessage(messageData);
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
        if (event.getState() == ServerKickEvent.State.CONNECTING) return;
        if (!event.getPlayer().getGroups().contains("successful-connection")) return;
        previousServerHandler.put(event.getPlayer().getName(), event.getKickedFrom());
    }

    @EventHandler
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        if (!event.getPlayer().getGroups().contains("successful-connection")) return;
        if (!event.getPlayer().getGroups().contains("not-first-join")) return;
        if (plugin.isVanishAPIEnabled() && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

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
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getConnection().getName();

        if (!plugin.getConfig().getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) return;
        if (!plugin.getBanHelper().isBanned(playerName)) return;

        event.setCancelled(true);
        event.setReason(Helper.convertToBungee("You are banned from the proxy.")[0]);
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        event.getPlayer().addGroups("successful-connection");
    }

    @EventHandler
    public void onPlayerJoinProxy(ServerConnectedEvent event) {
        if (event.getPlayer().getGroups().contains("not-first-join")) return;  // If not first join, don't do anything.
        if (!event.getPlayer().getGroups().contains("successful-connection")) return;
        event.getPlayer().addGroups("not-first-join");

        if (plugin.isVanishAPIEnabled() && BungeeVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        join(event.getPlayer(), event.getServer(), false);
    }

    public void join(ProxiedPlayer player, @Nullable Server server, boolean isFake) {
        // Bungee is "dumb" and needs to be delayed...
        try {
            plugin.getProxy().getScheduler().schedule(
                    plugin,
                    () -> {
                        if (server == null || server.getInfo() == null) return;

                        previousServerHandler.put(player.getName(), server.getInfo());

                        if (isFake) chatHandler.runProxyJoinMessage(player.getName(), player.getUniqueId(), server.getInfo().getName(), this::sendToAllServersVanish);
                        else chatHandler.runProxyJoinMessage(player.getName(), player.getUniqueId(), server.getInfo().getName(), this::sendToAllServers);
                    },
                    50L * 2, TimeUnit.MILLISECONDS);  // 50ms is 1 tick
        } catch (Exception e) {
            plugin.getLogger().warning("BungeeCord error. This is a bungeecord issue and cannot be fixed: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (plugin.isVanishAPIEnabled() && BungeeVanishAPI.isInvisible(player)) return;  // Ignore if player is invisible.
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
                        .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, Helper.convertToBungee(message))),
                (message) -> player.sendMessage(ChatMessageType.CHAT, Helper.convertToBungee(message))
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

    private void sendToAllServers(String parsedMessage, Permission permission) {
        plugin.getProxy().getPlayers().stream()
                .filter((player) -> {
                    if (plugin.getConfig().getAsBoolean(ConfigDataKey.USE_PERMISSIONS))
                        return player.hasPermission(permission.getPermissionNode());
                    return true;
                })
                .filter((player) -> {
                    if (player.getServer() == null || player.getServer().getInfo() == null) return false;
                    return !Helper.serverHasChatLocked(plugin, player.getServer().getInfo().getName());
                })
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, Helper.convertToBungee(parsedMessage)));
    }

    private void sendToAllServersVanish(String parsedMessage, Permission permission) {
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
                .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, Helper.convertToBungee(parsedMessage)));
    }

}
