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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatHandler {

    private static final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    private final Config config;
    private final Bot discordBot;

    private final Consumer<String> globalLogger;
    private final Consumer<String> pluginLogger;

    private final LuckPerms luckPermsAPI;

    public ChatHandler(Config config, Bot discordBot, Consumer<String> globalLogger,
                       Consumer<String> pluginLogger, LuckPerms luckPermsAPI) {
        this.config = config;
        this.discordBot = discordBot;

        this.globalLogger = globalLogger;
        this.pluginLogger = pluginLogger;
        discordBot.getJDA().ifPresent((jda) -> jda.addEventListener(new DiscordChatHandler(config, this::sendFromDiscord)));

        this.luckPermsAPI = luckPermsAPI;
    }

    public void runProxyChatMessage(String serverName, String playerName, UUID playerUUID,
                                    String playerMessage, Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
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

        if ((boolean) config.get(ConfigDataKey.LUCKPERMS_ENABLED)) {
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

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
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE);

        String message = configString.replace("%player%", playerName);
        String discordMessage = discordConfigString.replace("%player%", playerName);

        if ((boolean) config.get(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
        discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED).build());

        // Log to Minecraft
        minecraftLogger.accept(message);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID,
                                    Consumer<String> consoleLogger, Consumer<String> minecraftLogger) {
        String configString = (String) config.get(ConfigDataKey.JOIN_FORMAT);
        String discordConfigString = (String) config.get(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN);

        String message = configString.replace("%player%", playerName);
        String discordMessage = discordConfigString.replace("%player%", playerName);

        if ((boolean) config.get(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(message)));

        // Log to Discord
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

        if ((boolean) config.get(ConfigDataKey.LUCKPERMS_ENABLED)) {
            consoleMessage = replacePrefixSuffix(consoleMessage, playerUUID);
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID);
        }

        // Log to Console
        consoleLogger.accept(Helper.stripColor(MiniMessage.miniMessage().deserialize(consoleMessage)));

        // Log to Discord
        discordBot.sendMessageEmbed(simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW).build());

        // Log to Minecraft
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
        String message = (String) config.get(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE);

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
            User user = luckPermsAPI.getUserManager().loadUser(playerUUID).get();

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
