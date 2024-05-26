package com.beanbeanjuice.simpleproxychat.socket.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.listeners.MessageType;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeServerListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeCordPluginMessagingListener implements Listener {

    private final SimpleProxyChatBungee plugin;
    private final BungeeServerListener listener;

    public BungeeCordPluginMessagingListener(SimpleProxyChatBungee plugin, BungeeServerListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("BungeeCord")) return;

        ByteArrayDataInput input = ByteStreams.newDataInput(event.getData());

        if (!input.readUTF().equals("SimpleProxyChat")) return;

        MessageType type = MessageType.valueOf(input.readUTF());

        switch (type) {
            case CHAT -> runChat(input);
        }
    }

    private void runChat(ByteArrayDataInput input) {
        String serverName = input.readUTF();
        String playerName = input.readUTF();
        String playerMessage = input.readUTF();
        String parsedMinecraftString = input.readUTF();
        String parsedDiscordString = input.readUTF();
        String parsedDiscordEmbedTitle = input.readUTF();
        String parsedDiscordEmbedMessage = input.readUTF();

        ProxiedPlayer player = plugin.getProxy().getPlayer(playerName);
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);

        BungeeChatMessageData messageData = new BungeeChatMessageData(
                plugin, MessageType.CHAT, serverInfo, player, playerMessage, parsedMinecraftString, parsedDiscordString, parsedDiscordEmbedTitle, parsedDiscordEmbedMessage
        );

        this.listener.getChatHandler().chat(
                messageData,
                Helper.translateLegacyCodes(parsedMinecraftString),
                Helper.translateLegacyCodes(parsedDiscordString),
                Helper.translateLegacyCodes(parsedDiscordEmbedTitle),
                Helper.translateLegacyCodes(parsedDiscordEmbedMessage)
        );
    }

}
