package com.beanbeanjuice.simpleproxychat.socket;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.shared.socket.ChatMessageData;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.beanbeanjuice.simpleproxychat.shared.utility.listeners.MessageType;
import com.beanbeanjuice.simpleproxychat.utility.listeners.VelocityServerListener;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Collection;

public class VelocityChatMessageData extends ChatMessageData {

    private SimpleProxyChatVelocity plugin;
    @Getter private final RegisteredServer server;

    public VelocityChatMessageData(SimpleProxyChatVelocity plugin, MessageType type, RegisteredServer server,
                                   Player player, String message) {
        super(
                type,
                server.getServerInfo().getName(),
                player.getUsername(),
                player.getUniqueId(),
                message
        );
        this.plugin = plugin;
        this.server = server;
    }

    public VelocityChatMessageData(SimpleProxyChatVelocity plugin, MessageType type, RegisteredServer server,
                                 Player player, String message,
                                 String parsedMinecraftString, String parsedDiscordString, String parsedDiscordEmbedTitle, String parsedDiscordEmbedMessage) {
        super(
                type,
                server.getServerInfo().getName(),
                player.getUsername(),
                player.getUniqueId(),
                message,
                parsedMinecraftString,
                parsedDiscordString,
                parsedDiscordEmbedTitle,
                parsedDiscordEmbedMessage
        );

        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void chatSendToAllOtherPlayers(String parsedMessage) {
        Collection<Player> blacklistedUUIDs = server.getPlayersConnected();

        Component component = MiniMessage.miniMessage().deserialize(parsedMessage);

        plugin.getProxyServer().getAllPlayers().stream()
                .filter((streamPlayer) -> !blacklistedUUIDs.contains(streamPlayer))
                .filter((streamPlayer) -> {
                    if (!plugin.getConfig().get(ConfigKey.USE_PERMISSIONS).asBoolean()) return true;
                    return streamPlayer.hasPermission(Permission.READ_CHAT_MESSAGE.getPermissionNode());
                })
                .filter((streamPlayer) -> !VelocityServerListener.playerIsInDisabledServer(streamPlayer, plugin))
                .forEach((streamPlayer) -> streamPlayer.sendMessage(component));

    }

    @Override
    public void startPluginMessage() {
        server.sendPluginMessage(VelocityPluginMessagingListener.IDENTIFIER, this.getAsBytes());
    }

}
