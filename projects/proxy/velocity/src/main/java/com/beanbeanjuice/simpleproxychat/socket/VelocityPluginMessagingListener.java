package com.beanbeanjuice.simpleproxychat.socket;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.shared.utility.listeners.MessageType;
import com.beanbeanjuice.simpleproxychat.utility.listeners.VelocityServerListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class VelocityPluginMessagingListener {

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("custom:spc");
    private final SimpleProxyChatVelocity plugin;
    private final VelocityServerListener listener;

    public VelocityPluginMessagingListener(final SimpleProxyChatVelocity plugin, final VelocityServerListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Subscribe
    public void onPluginMessageFromPlayer(PluginMessageEvent event) {
        if (event.getIdentifier() != IDENTIFIER) return;

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

        Optional<Player> player = plugin.getProxyServer().getPlayer(playerName);
        Optional<RegisteredServer> server = plugin.getProxyServer().getServer(serverName);

        if (player.isEmpty() || server.isEmpty()) return;

        VelocityChatMessageData messageData = new VelocityChatMessageData(
                plugin, MessageType.CHAT, server.get(), player.get(), playerMessage, parsedMinecraftString, parsedDiscordString, parsedDiscordEmbedTitle, parsedDiscordEmbedMessage
        );

        this.listener.getChatHandler().chat(
                messageData,
                CommonHelper.translateLegacyCodes(parsedMinecraftString),
                CommonHelper.translateLegacyCodes(parsedDiscordString),
                CommonHelper.translateLegacyCodes(parsedDiscordEmbedTitle),
                CommonHelper.translateLegacyCodes(parsedDiscordEmbedMessage)
        );
    }

}
