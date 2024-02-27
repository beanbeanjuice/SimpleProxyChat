package com.beanbeanjuice.simpleproxychat.chat;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.discord.DiscordChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatHandler {

    private static final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    private Config config;
    private Bot discordBot;

    private Consumer<String> globalLogger;

    public ChatHandler(Config config, Bot discordBot, Consumer<String> globalLogger) {
        this.config = config;
        this.discordBot = discordBot;

        this.globalLogger = globalLogger;

        discordBot.getBot().addEventListener(new DiscordChatHandler(config, this::sendFromDiscord));
    }

    public void runProxyChatMessage(String serverName, String playerName, String playerMessage,
                                    Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String minecraftConfigString = (String) config.get(ConfigDataKey.MESSAGE_FORMAT);
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_MESSAGE);

        serverName = serverName.toUpperCase();

        String minecraftMessage = minecraftConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        String discordMessage = discordConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(minecraftMessage)));

        // Log to Discord
        discordBot.sendMessage(discordMessage);

        // Log to Minecraft
        minecraftLogger.accept(minecraftMessage);
    }

    public void runProxyLeaveMessage(String playerName, UUID playerUUID,
                                     Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String configString = (String) config.get(ConfigDataKey.LEAVE_FORMAT);

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED).build());

        // Log to Minecraft
        minecraftLogger.accept(message);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID,
                                    Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String configString = (String) config.get(ConfigDataKey.JOIN_FORMAT);

        String message = configString
                .replace("%player%", playerName);

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN);
        String discordMessage = discordConfigString.replace("%player%", playerName);
        discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.GREEN).build());

        // Log to Minecraft
        minecraftLogger.accept(message);
    }

    public void runProxySwitchMessage(String from, String to, String playerName, UUID playerUUID,
                                      Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String consoleConfigString = (String) config.get(ConfigDataKey.SWITCH_FORMAT);
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_SWITCH);
        String minecraftConfigString = (String) config.get(ConfigDataKey.SWITCH_FORMAT_NO_FROM);

        from = from.toUpperCase();
        to = to.toUpperCase();

        String consoleMessage = consoleConfigString
                .replace("%from%", from)
                .replace("%to%", to)
                .replace("%player%", playerName);

        String discordMessage = discordConfigString
                .replace("%from%", from)
                .replace("%to%", to)
                .replace("%player%", playerName);

        String minecraftMessage = minecraftConfigString
                .replace("%from%", from)
                .replace("%to%", to)
                .replace("%player%", playerName);

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(consoleMessage)));

        // Log to Discord
        discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW).build());

        // Log to Minecraft
        minecraftLogger.accept(minecraftMessage);
    }

    private EmbedBuilder simpleAuthorEmbedBuilder(@NotNull UUID playerUUID, @NotNull String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message, null, getPlayerHeadURL(playerUUID));
        return embedBuilder;
    }

    private String getPlayerHeadURL(@NotNull UUID playerUUID) {
        return MINECRAFT_PLAYER_HEAD_URL.replace("{PLAYER_UUID}", playerUUID.toString());
    }

    public void sendFromDiscord(MessageReceivedEvent event) {
        String message = (String) config.get(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE);

        String username = event.getMember().getEffectiveName();

        String roleName = "[no-role]";
        Color roleColor = Color.GRAY;
        if (!event.getMember().getRoles().isEmpty()) {
            Role role = event.getMember().getRoles().get(0);
            roleName = role.getName();

            if (role.getColor() != null) roleColor = role.getColor();
        }

        String discordMessage = event.getMessage().getContentStripped();

        String hex = "#" + Integer.toHexString(roleColor.getRGB()).substring(2);
        message = message
                .replace("%role%", String.format("<%s>%s</%s>", hex, roleName, hex))
                .replace("%user%", username)
                .replace("%message%", discordMessage);

        globalLogger.accept(message);
    }

}
