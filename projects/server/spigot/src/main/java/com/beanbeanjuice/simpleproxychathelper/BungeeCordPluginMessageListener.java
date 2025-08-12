package com.beanbeanjuice.simpleproxychathelper;

import com.beanbeanjuice.simpleproxychathelper.shared.config.ConfigKey;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class BungeeCordPluginMessageListener implements PluginMessageListener {

    private final SimpleProxyChatHelper plugin;

    public BungeeCordPluginMessageListener(SimpleProxyChatHelper plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord") && !channel.equals("custom:spc")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (!subChannel.equals(SimpleProxyChatHelper.getSubChannel())) return;

        MessageType type = MessageType.valueOf(in.readUTF());

        switch (type) {
            case CHAT:
                handleChatMessage(type, in, channel);
                break;
        }
    }

    private void handleChatMessage(MessageType type, ByteArrayDataInput in, String channel) {
        String serverName = in.readUTF();
        String playerName = in.readUTF();
        String playerMessage = in.readUTF();
        String minecraftMessage = in.readUTF();  // The config message to parse.
        String discordMessage = in.readUTF();
        String discordEmbedTitle = in.readUTF();
        String discordEmbedMessage = in.readUTF();

        Player playerForParsing = Bukkit.getPlayer(playerName);
        if (playerForParsing == null) return;

        if (plugin.getOptions().getOption(ConfigKey.PLACEHOLDER_API_SUPPORT)) {
            minecraftMessage = PlaceholderAPI.setPlaceholders(playerForParsing, minecraftMessage);
            discordMessage = PlaceholderAPI.setPlaceholders(playerForParsing, discordMessage);
            discordEmbedTitle = PlaceholderAPI.setPlaceholders(playerForParsing, discordEmbedTitle);
            discordEmbedMessage = PlaceholderAPI.setPlaceholders(playerForParsing, discordEmbedMessage);
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(SimpleProxyChatHelper.getSubChannel());
        out.writeUTF(type.name());
        out.writeUTF(serverName);
        out.writeUTF(playerName);
        out.writeUTF(playerMessage);
        out.writeUTF(minecraftMessage);
        out.writeUTF(discordMessage);
        out.writeUTF(discordEmbedTitle);
        out.writeUTF(discordEmbedMessage);

        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(plugin, channel, out.toByteArray());
    }

}
