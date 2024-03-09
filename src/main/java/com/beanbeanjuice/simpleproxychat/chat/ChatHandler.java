package com.beanbeanjuice.simpleproxychat.chat;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.discord.DiscordChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatHandler {

    private static final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    private final Config config;
    private final Bot discordBot;

    private final Consumer<String> globalLogger;
    private final Consumer<String> pluginLogger;

    public ChatHandler(Config config, Bot discordBot, Consumer<String> globalLogger,
                       Consumer<String> pluginLogger) {
        this.config = config;
        this.discordBot = discordBot;

        this.globalLogger = globalLogger;
        this.pluginLogger = pluginLogger;
        discordBot.getJDA().ifPresent((jda) -> jda.addEventListener(new DiscordChatHandler(config, this::sendFromDiscord)));
    }

    public void runProxyChatMessage(String serverName, String playerName, UUID playerUUID,
                                    String playerMessage, Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String minecraftConfigString = config.getAsString(ConfigDataKey.MINECRAFT_MESSAGE);
        String discordConfigString = config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_MESSAGE);

        serverName = Helper.convertAlias(config, serverName);

        String minecraftMessage = minecraftConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        String discordMessage = discordConfigString
                .replace("%message%", playerMessage)
                .replace("%server%", serverName)
                .replace("%player%", playerName);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(minecraftMessage)));

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE)) {
            String title = config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_EMBED_TITLE)
                            .replace("%server%", serverName)
                            .replace("%player%", playerName);

            title = replacePrefixSuffix(title, playerUUID);
            Color color = config.getAsColor(ConfigDataKey.MINECRAFT_DISCORD_EMBED_COLOR).orElse(Color.RED);
            discordBot.sendMessageEmbed(
                    new EmbedBuilder()
                            .setAuthor(title, null, getPlayerHeadURL(playerUUID))
                            .setDescription(playerMessage)
                            .setColor(color)
                            .build()
            );
        } else {
            discordBot.sendMessage(discordMessage);
        }

        // Log to Minecraft
        minecraftLogger.accept(minecraftMessage);
    }

    public void runProxyLeaveMessage(String playerName, UUID playerUUID,
                                     Consumer<String> consoleLogger, BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_LEAVE);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_LEAVE);

        String message = configString.replace("%player%", playerName);
        String discordMessage = discordConfigString.replace("%player%", playerName);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_LEAVE_USE))
            discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED).build());

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_LEAVE_USE))
            minecraftLogger.accept(message, Permission.READ_LEAVE_MESSAGE);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID,
                                    Consumer<String> consoleLogger, BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_JOIN);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_JOIN);

        String message = configString.replace("%player%", playerName);
        String discordMessage = discordConfigString.replace("%player%", playerName);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_JOIN_USE))
            discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.GREEN).build());

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_JOIN_USE))
            minecraftLogger.accept(message, Permission.READ_JOIN_MESSAGE);
    }

    public void runProxySwitchMessage(String from, String to, String playerName, UUID playerUUID,
                                      Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String consoleConfigString = config.getAsString(ConfigDataKey.MINECRAFT_SWITCH_DEFAULT);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_SWITCH);
        String minecraftConfigString = config.getAsString(ConfigDataKey.MINECRAFT_SWITCH_SHORT);

        from = Helper.convertAlias(config, from);
        to = Helper.convertAlias(config, to);

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

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            consoleMessage = replacePrefixSuffix(consoleMessage, playerUUID);
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(consoleMessage)));

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_SWITCH_USE))
            discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW).build());

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_SWITCH_USE))
            minecraftLogger.accept(minecraftMessage);
    }

    private EmbedBuilder simpleAuthorEmbedBuilder(@NotNull UUID playerUUID, @NotNull String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)), null, getPlayerHeadURL(playerUUID));
        return embedBuilder;
    }

    private String getPlayerHeadURL(@NotNull UUID playerUUID) {
        return MINECRAFT_PLAYER_HEAD_URL.replace("{PLAYER_UUID}", playerUUID.toString());
    }

    public void sendFromDiscord(MessageReceivedEvent event) {
        String message = config.getAsString(ConfigDataKey.DISCORD_MINECRAFT_MESSAGE);

        if (event.getMember() == null) return;

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

    private String replacePrefixSuffix(String message, UUID playerUUID) {
        try {
            User user = LuckPermsProvider.get().getUserManager().loadUser(playerUUID).get();

            List<String> prefixList = user.resolveInheritedNodes(QueryOptions.nonContextual())
                    .stream()
                    .filter(NodeType.PREFIX::matches)
                    .map(NodeType.PREFIX::cast)
                    .map(PrefixNode::getKey)
                    .map(prefix -> prefix.replace("prefix.", ""))
                    .sorted((left, right) -> Character.compare(right.charAt(0), left.charAt(0)))
                    .map(prefix -> prefix.split("\\.")[1])
                    .toList();

            List<String> suffixList = user.resolveInheritedNodes(QueryOptions.nonContextual())
                    .stream()
                    .filter(NodeType.SUFFIX::matches)
                    .map(NodeType.SUFFIX::cast)
                    .map(SuffixNode::getKey)
                    .map(suffix -> suffix.replace("suffix.", ""))
                    .sorted((left, right) -> Character.compare(right.charAt(0), left.charAt(0)))
                    .map(suffix -> suffix.split("\\.")[1])
                    .toList();

            String prefix = prefixList.isEmpty() ? "" : Helper.translateLegacyCodes(prefixList.get(0));
            String suffix = suffixList.isEmpty() ? "" : Helper.translateLegacyCodes(suffixList.get(0));

            return message.replace("%prefix%", prefix).replace("%suffix%", suffix);
        } catch (Exception e) {
            pluginLogger.accept("There was an error contacting the LuckPerms API: " + e.getMessage());
            return message;
        }
    }

}
