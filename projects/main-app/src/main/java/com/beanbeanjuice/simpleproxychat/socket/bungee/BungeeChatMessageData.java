package com.beanbeanjuice.simpleproxychat.socket.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.socket.ChatMessageData;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.listeners.MessageType;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;

import static com.beanbeanjuice.simpleproxychat.utility.helper.Helper.convertToBungee;

public class BungeeChatMessageData extends ChatMessageData {

    private final SimpleProxyChatBungee plugin;
    @Getter private final ServerInfo serverInfo;

    public BungeeChatMessageData(SimpleProxyChatBungee plugin, MessageType type, ServerInfo serverInfo,
                                 ProxiedPlayer player, String message) {
        super(
                type,
                serverInfo.getName(),
                player.getName(),
                player.getUniqueId(),
                message
        );

        this.plugin = plugin;
        this.serverInfo = serverInfo;
    }

    public BungeeChatMessageData(SimpleProxyChatBungee plugin, MessageType type, ServerInfo serverInfo,
                                 ProxiedPlayer player, String message,
                                 String parsedMinecraftString, String parsedDiscordString, String parsedDiscordEmbedTitle, String parsedDiscordEmbedMessage) {
        super(
                type,
                serverInfo.getName(),
                player.getName(),
                player.getUniqueId(),
                message,
                parsedMinecraftString,
                parsedDiscordString,
                parsedDiscordEmbedTitle,
                parsedDiscordEmbedMessage
        );

        this.plugin = plugin;
        this.serverInfo = serverInfo;
    }

    @Override
    public void chatSendToAllOtherPlayers(String parsedMessage) {
        Collection<ProxiedPlayer> blacklistedPlayers = serverInfo.getPlayers();

        plugin.getProxy().getPlayers().stream()
                .filter((streamPlayer) -> !blacklistedPlayers.contains(streamPlayer))
                .filter((streamPlayer) -> {
                    if (blacklistedPlayers.contains(streamPlayer)) return false;
                    if (blacklistedPlayers.stream().map(ProxiedPlayer::getName).toList().contains(streamPlayer.getName())) return false;
                    if (blacklistedPlayers.stream().map(ProxiedPlayer::getUniqueId).toList().contains(streamPlayer.getUniqueId())) return false;

                    return true;
                })
                .filter((streamPlayer) -> {
                    if (!plugin.getConfig().get(ConfigKey.USE_PERMISSIONS).asBoolean()) return true;
                    return streamPlayer.hasPermission(Permission.READ_CHAT_MESSAGE.getPermissionNode());
                })
                .forEach((streamPlayer) -> streamPlayer.sendMessage(ChatMessageType.CHAT, convertToBungee(parsedMessage)));
    }

    @Override
    public void startPluginMessage() {
        serverInfo.sendData("BungeeCord", this.getAsBytes());
    }

}
