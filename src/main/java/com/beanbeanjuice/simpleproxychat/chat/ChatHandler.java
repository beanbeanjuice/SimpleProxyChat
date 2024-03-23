package com.beanbeanjuice.simpleproxychat.chat;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.discord.DiscordChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                                    String playerMessage, Consumer<String> minecraftLogger) {
        String minecraftConfigString = config.getAsString(ConfigDataKey.MINECRAFT_MESSAGE);
        String discordConfigString = config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.create("message", playerMessage));
        replacements.add(Tuple.create("server", aliasedServerName));
        replacements.add(Tuple.create("original_server", serverName));
        replacements.add(Tuple.create("to", aliasedServerName));
        replacements.add(Tuple.create("original_to", serverName));
        replacements.add(Tuple.create("player", playerName));
        replacements.add(Tuple.create("epoch", String.valueOf(Instant.now().getEpochSecond())));
        replacements.add(Tuple.create("time", getTimeString()));

        String minecraftMessage = replaceKeys(minecraftConfigString, replacements);
        String discordMessage = replaceKeys(discordConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        }

        // Log to Console
        pluginLogger.accept(minecraftMessage);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE)) {
            String title = replaceKeys(config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_EMBED_TITLE), replacements);
            String message = replaceKeys(config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_EMBED_MESSAGE), replacements);

            title = replacePrefixSuffix(title, playerUUID, aliasedServerName, serverName);

            Color color = config.getAsColor(ConfigDataKey.MINECRAFT_DISCORD_EMBED_COLOR).orElse(Color.RED);

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(title, null, getPlayerHeadURL(playerUUID))
                    .setDescription(message)
                    .setColor(color);

            if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP))
                embedBuilder.setTimestamp(Instant.now());

            discordBot.sendMessageEmbed(embedBuilder.build());
        } else {
            discordBot.sendMessage(discordMessage);
        }

        // Log to Minecraft
        minecraftLogger.accept(minecraftMessage);
    }

    public void runProxyLeaveMessage(String playerName, UUID playerUUID, String serverName,
                                     BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_LEAVE);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_LEAVE_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.create("player", playerName));
        replacements.add(Tuple.create("server", aliasedServerName));
        replacements.add(Tuple.create("original_server", serverName));
        replacements.add(Tuple.create("to", aliasedServerName));
        replacements.add(Tuple.create("original_to", serverName));
        replacements.add(Tuple.create("epoch", String.valueOf(Instant.now().getEpochSecond())));
        replacements.add(Tuple.create("time", getTimeString()));

        String message = replaceKeys(configString, replacements);
        String discordMessage = replaceKeys(discordConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        }

        // Log to Console
        pluginLogger.accept(message);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_LEAVE_USE)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_LEAVE_USE_TIMESTAMP)) embedBuilder.setTimestamp(Instant.now());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_LEAVE_USE))
            minecraftLogger.accept(message, Permission.READ_LEAVE_MESSAGE);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID, String serverName,
                                    BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_JOIN);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_JOIN_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.create("player", playerName));
        replacements.add(Tuple.create("server", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.create("to", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.create("server", aliasedServerName));
        replacements.add(Tuple.create("original_server", serverName));
        replacements.add(Tuple.create("to", aliasedServerName));
        replacements.add(Tuple.create("original_to", serverName));
        replacements.add(Tuple.create("epoch", String.valueOf(Instant.now().getEpochSecond())));
        replacements.add(Tuple.create("time", getTimeString()));

        String message = replaceKeys(configString, replacements);
        String discordMessage = replaceKeys(discordConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        }

        // Log to Console
        pluginLogger.accept(message);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_JOIN_USE)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.GREEN);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_JOIN_USE_TIMESTAMP)) embedBuilder.setTimestamp(Instant.now());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_JOIN_USE))
            minecraftLogger.accept(message, Permission.READ_JOIN_MESSAGE);
    }

    public void runProxySwitchMessage(String from, String to, String playerName, UUID playerUUID,
                                      Consumer<String> minecraftLogger) {
        String consoleConfigString = config.getAsString(ConfigDataKey.MINECRAFT_SWITCH_DEFAULT);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_SWITCH_MESSAGE);
        String minecraftConfigString = config.getAsString(ConfigDataKey.MINECRAFT_SWITCH_SHORT);

        String aliasedFrom = Helper.convertAlias(config, from);
        String aliasedTo = Helper.convertAlias(config, to);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.create("from", aliasedFrom));
        replacements.add(Tuple.create("original_from", from));
        replacements.add(Tuple.create("to", aliasedTo));
        replacements.add(Tuple.create("original_to", to));
        replacements.add(Tuple.create("server", aliasedTo));
        replacements.add(Tuple.create("original_server", to));
        replacements.add(Tuple.create("player", playerName));
        replacements.add(Tuple.create("epoch", String.valueOf(Instant.now().getEpochSecond())));
        replacements.add(Tuple.create("time", getTimeString()));

        String consoleMessage = replaceKeys(consoleConfigString, replacements);
        String discordMessage = replaceKeys(discordConfigString, replacements);
        String minecraftMessage = replaceKeys(minecraftConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            consoleMessage = replacePrefixSuffix(consoleMessage, playerUUID, aliasedTo, to);
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedTo, to);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedTo, to);
        }

        // Log to Console
        pluginLogger.accept(consoleMessage);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_SWITCH_USE)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_SWITCH_USE_TIMESTAMP)) embedBuilder.setTimestamp(Instant.now());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_SWITCH_USE))
            minecraftLogger.accept(minecraftMessage);
    }

    /**
     * Creates a sanitized {@link EmbedBuilder} based on the message.
     * @param playerUUID The {@link UUID} of the in-game player.
     * @param message The {@link String} message to send in the Discord server.
     * @return A sanitized {@link EmbedBuilder} containing the contents.
     */
    private EmbedBuilder simpleAuthorEmbedBuilder(@NotNull UUID playerUUID, @NotNull String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message, null, getPlayerHeadURL(playerUUID));
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

        message = replaceKeys(
                message,
                Tuple.create("role", String.format("<%s>%s</%s>", hex, roleName, hex)),
                Tuple.create("user", username),
                Tuple.create("message", discordMessage),
                Tuple.create("epoch", String.valueOf(Instant.now().getEpochSecond())),
                Tuple.create("time", getTimeString())
        );

        globalLogger.accept(message);
    }

    private String replaceKeys(String string, List<Tuple<String, String>> entries) {
        for (Tuple<String, String> entry : entries)
            string = string.replaceAll(String.format("%%%s%%", entry.getKey()), entry.getValue());

        return string;
    }

    @SafeVarargs
    private String replaceKeys(String string, Tuple<String, String>... entries) {
        for (Tuple<String, String> entry : entries)
            string = string.replaceAll(String.format("%%%s%%", entry.getKey()), entry.getValue());

        return string;
    }

    private List<String> getPrefixBasedOnServerContext(User user, String serverKey) {
        Stream<Node> prefixStream = user.resolveInheritedNodes(QueryOptions.nonContextual()).stream();

        if (!serverKey.isEmpty()) prefixStream = prefixStream.filter((node) -> node.getContexts().contains("server", serverKey));

        return prefixStream
                .filter(Node::getValue)
                .filter(NodeType.PREFIX::matches)
                .map(NodeType.PREFIX::cast)
                .map(PrefixNode::getKey)
                .map(prefix -> prefix.replace("prefix.", "")) // 200.Owner.is.awesome
                .map(prefix -> prefix.split("\\."))  // [200, Owner, is, awesome]
                .sorted((left, right) -> {  // Sorting it properly.
                    try {
                        Integer leftWeight = Integer.parseInt(left[0]);
                        Integer rightWeight = Integer.parseInt(right[0]);

                        return rightWeight.compareTo(leftWeight);
                    } catch (NumberFormatException e) { return 0; }
                })
                .map(prefix -> Arrays.stream(prefix).skip(1).collect(Collectors.joining(".")))  // Owner.is.awesome
                .toList();
    }

    private List<String> getSuffixBasedOnServerContext(User user, String serverKey) {
        Stream<Node> suffixStream = user.resolveInheritedNodes(QueryOptions.nonContextual()).stream();

        if (!serverKey.isEmpty()) suffixStream = suffixStream.filter((node) -> node.getContexts().contains("server", serverKey));

        return suffixStream
                .filter(Node::getValue)
                .filter(NodeType.SUFFIX::matches)
                .map(NodeType.SUFFIX::cast)
                .map(SuffixNode::getKey)
                .map(suffix -> suffix.replace("suffix.", "")) // 200.Owner.is.awesome
                .map(suffix -> suffix.split("\\."))  // [200, Owner, is, awesome]
                .sorted((left, right) -> {  // Sorting it properly.
                    try {
                        Integer leftWeight = Integer.parseInt(left[0]);
                        Integer rightWeight = Integer.parseInt(right[0]);

                        return rightWeight.compareTo(leftWeight);
                    } catch (NumberFormatException e) { return 0; }
                })
                .map(suffix -> Arrays.stream(suffix).skip(1).collect(Collectors.joining(".")))  // Owner.is.awesome
                .toList();
    }

    private String replacePrefixSuffix(String message, UUID playerUUID, String aliasedServerName, String serverName) {
        try {
            User user = LuckPermsProvider.get().getUserManager().loadUser(playerUUID).get();

            // Get prefix based on aliased name. If none show up, use original name. If none show up, use top prefix.
            List<String> prefixList = getPrefixBasedOnServerContext(user, aliasedServerName);
            if (prefixList.isEmpty()) prefixList = getPrefixBasedOnServerContext(user, serverName);
            if (prefixList.isEmpty()) prefixList = getPrefixBasedOnServerContext(user, "");

            List<String> suffixList = getSuffixBasedOnServerContext(user, aliasedServerName);
            if (suffixList.isEmpty()) suffixList = getSuffixBasedOnServerContext(user, serverName);
            if (suffixList.isEmpty()) suffixList = getSuffixBasedOnServerContext(user, "");

            String prefix = prefixList.isEmpty() ? "" : Helper.translateLegacyCodes(prefixList.get(0));
            String suffix = suffixList.isEmpty() ? "" : Helper.translateLegacyCodes(suffixList.get(0));

            return message.replace("%prefix%", prefix).replace("%suffix%", suffix);
        } catch (Exception e) {
            pluginLogger.accept("There was an error contacting the LuckPerms API: " + e.getMessage());
            return message;
        }
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">Format</a>
     */
    private String getTimeString() {
        DateTimeZone zone = DateTimeZone.forID(config.getAsString(ConfigDataKey.TIMESTAMP_TIMEZONE));
        DateTimeFormatter format = DateTimeFormat.forPattern(config.getAsString(ConfigDataKey.TIMESTAMP_FORMAT));

        long timeInMillis = System.currentTimeMillis();
        DateTime time = new DateTime(timeInMillis).withZone(zone);

        return time.toString(format);
    }

}
