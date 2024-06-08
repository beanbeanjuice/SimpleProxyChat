package com.beanbeanjuice.simpleproxychat.utility.listeners.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.socket.velocity.VelocityChatMessageData;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.listeners.MessageType;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityServerListener {

    @Getter private ServerStatusManager serverStatusManager;
    private final SimpleProxyChatVelocity plugin;
    @Getter private final ChatHandler chatHandler;
    private VelocityVanishListener velocityVanishListener;

    public VelocityServerListener(SimpleProxyChatVelocity plugin, ChatHandler chatHandler) {
        this.plugin = plugin;
        this.chatHandler = chatHandler;
        startServerStatusDetection();
    }

    public void initializeVelocityVanishListener() {
        this.velocityVanishListener = new VelocityVanishListener(plugin, this);
        this.velocityVanishListener.startVanishListener();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        String playerMessage = event.getMessage();
        Player player = event.getPlayer();
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(player)) {
            // If is allowed to speak in vanish, continue.
            if (!event.getMessage().endsWith("/")) {
                String errorMessage = plugin.getConfig().getAsString(ConfigDataKey.MINECRAFT_CHAT_VANISHED_MESSAGE);
                player.sendMessage(Helper.stringToComponent(errorMessage));
                return;
            }
            playerMessage = playerMessage.substring(0, playerMessage.length() - 1);
        }
        if (!Helper.playerCanChat(plugin.getConfig(), player.getUniqueId(), player.getUsername())) return;

        String finalPlayerMessage = playerMessage;
        event.getPlayer().getCurrentServer().ifPresent((connection) -> {
            VelocityChatMessageData messageData = new VelocityChatMessageData(plugin, MessageType.CHAT, connection.getServer(), player, finalPlayerMessage);
            chatHandler.runProxyChatMessage(messageData);
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(event.getPlayer())) return;  // Ignore if invisible.

        leave(event.getPlayer());
    }

    protected void leave(Player player) {
        String serverName = "no-server";
        if (player.getCurrentServer().isPresent())
            serverName = player.getCurrentServer().get().getServerInfo().getName();
        chatHandler.runProxyLeaveMessage(player.getUsername(), player.getUniqueId(), serverName, this::sendToAllServers);
    }

    protected void join(Player player, String serverName) {
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
    public void onPreLoginEvent(PreLoginEvent event) {
        String playerName = event.getUsername();

        if (!plugin.getConfig().getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) return;
        if (!plugin.getBanHelper().isBanned(playerName)) return;

        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Helper.stringToComponent("&cYou are banned from the proxy.")));
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED) && VelocityVanishAPI.isInvisible(event.getPlayer())) return;

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
