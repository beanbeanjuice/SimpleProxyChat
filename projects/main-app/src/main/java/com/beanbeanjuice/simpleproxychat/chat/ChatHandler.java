package com.beanbeanjuice.simpleproxychat.chat;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.discord.DiscordChatHandler;
import com.beanbeanjuice.simpleproxychat.socket.ChatMessageData;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChatHandler {

    private static final String MINECRAFT_PLAYER_HEAD_URL = "https://crafthead.net/avatar/{PLAYER_UUID}";

    private final Config config;
    private final EpochHelper epochHelper;
    private final Bot discordBot;

    private final Consumer<String> globalLogger;
    private final Consumer<String> pluginLogger;

    public ChatHandler(Config config, EpochHelper epochHelper, Bot discordBot,
                       Consumer<String> globalLogger, Consumer<String> pluginLogger) {
        this.config = config;
        this.epochHelper = epochHelper;
        this.discordBot = discordBot;

        this.globalLogger = globalLogger;
        this.pluginLogger = pluginLogger;
        discordBot.addRunnableToQueue(() -> discordBot.getJDA().ifPresent((jda) -> jda.addEventListener(new DiscordChatHandler(config, this::sendFromDiscord))));
    }

    private Optional<String> getValidMessage(String message) {
        String messagePrefix = config.getAsString(ConfigDataKey.PROXY_MESSAGE_PREFIX);

        if (messagePrefix.isEmpty()) return Optional.of(message);
        if (!message.startsWith(messagePrefix)) return Optional.empty();

        message = message.substring(messagePrefix.length());
        if (message.isEmpty()) return Optional.empty();
        return Optional.of(message);
    }

    public void chat(ChatMessageData chatMessageData, String minecraftMessage, String discordMessage, String discordEmbedTitle, String discordEmbedMessage) {
        // Log to Console
        if (config.getAsBoolean(ConfigDataKey.CONSOLE_CHAT)) pluginLogger.accept(minecraftMessage);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_ENABLED)) {
            if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE)) {

                Color color = config.getAsColor(ConfigDataKey.MINECRAFT_DISCORD_EMBED_COLOR).orElse(Color.RED);

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(discordEmbedTitle, null, getPlayerHeadURL(chatMessageData.getPlayerUUID()))
                        .setDescription(discordEmbedMessage)
                        .setColor(color);

                if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP))
                    embedBuilder.setTimestamp(epochHelper.getEpochInstant());

                discordBot.sendMessageEmbed(embedBuilder.build());
            } else {
                discordBot.sendMessage(discordMessage);
            }
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_CHAT_ENABLED)) chatMessageData.chatSendToAllOtherPlayers(minecraftMessage);

    }

    public void runProxyChatMessage(ChatMessageData chatMessageData) {
        if (Helper.serverHasChatLocked(config, chatMessageData.getServername())) return;

        String playerMessage = chatMessageData.getMessage();
        String serverName = chatMessageData.getServername();
        String playerName = chatMessageData.getPlayerName();
        UUID playerUUID = chatMessageData.getPlayerUUID();

        Optional<String> optionalPlayerMessage = getValidMessage(playerMessage);
        if (optionalPlayerMessage.isEmpty()) return;
        playerMessage = optionalPlayerMessage.get();

        String minecraftConfigString = config.getAsString(ConfigDataKey.MINECRAFT_CHAT_MESSAGE);
        String discordConfigString = config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("message", playerMessage));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("epoch", String.valueOf(epochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        String minecraftMessage = Helper.replaceKeys(minecraftConfigString, replacements);
        String discordMessage = Helper.replaceKeys(discordConfigString, replacements);
        String discordEmbedTitle = Helper.replaceKeys(config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_EMBED_TITLE), replacements);
        String discordEmbedMessage = Helper.replaceKeys(config.getAsString(ConfigDataKey.MINECRAFT_DISCORD_EMBED_MESSAGE), replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
            discordEmbedTitle = replacePrefixSuffix(discordEmbedTitle, chatMessageData.getPlayerUUID(), aliasedServerName, chatMessageData.getServername());
        }

        if (config.getAsBoolean(ConfigDataKey.USE_HELPER)) {
            chatMessageData.setMinecraftMessage(minecraftMessage);
            chatMessageData.setDiscordMessage(discordMessage);
            chatMessageData.setDiscordEmbedTitle(discordEmbedTitle);
            chatMessageData.setDiscordEmbedMessage(discordEmbedMessage);
            chatMessageData.startPluginMessage();
            return;
        }

        chat(chatMessageData, minecraftMessage, discordMessage, discordEmbedTitle, discordEmbedMessage);
    }
    public void runProxyLeaveMessage(String playerName, UUID playerUUID, String serverName,
                                     BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_LEAVE);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_LEAVE_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("epoch", String.valueOf(epochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        String message = Helper.replaceKeys(configString, replacements);
        String discordMessage = Helper.replaceKeys(discordConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        }

        // Log to Console
        if (config.getAsBoolean(ConfigDataKey.CONSOLE_LEAVE)) pluginLogger.accept(message);


        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_LEAVE_ENABLED)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_LEAVE_USE_TIMESTAMP)) embedBuilder.setTimestamp(epochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_LEAVE_ENABLED)) minecraftLogger.accept(message, Permission.READ_LEAVE_MESSAGE);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID, String serverName,
                                    BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.getAsString(ConfigDataKey.MINECRAFT_JOIN);
        String discordConfigString = config.getAsString(ConfigDataKey.DISCORD_JOIN_MESSAGE);

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("server", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.of("to", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("epoch", String.valueOf(epochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        String message = Helper.replaceKeys(configString, replacements);
        String discordMessage = Helper.replaceKeys(discordConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        }

        // Log to Console
        if (config.getAsBoolean(ConfigDataKey.CONSOLE_JOIN)) pluginLogger.accept(message);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_JOIN_ENABLED)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.GREEN);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_JOIN_USE_TIMESTAMP)) embedBuilder.setTimestamp(epochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_JOIN_ENABLED))
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
        replacements.add(Tuple.of("from", aliasedFrom));
        replacements.add(Tuple.of("original_from", from));
        replacements.add(Tuple.of("to", aliasedTo));
        replacements.add(Tuple.of("original_to", to));
        replacements.add(Tuple.of("server", aliasedTo));
        replacements.add(Tuple.of("original_server", to));
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("epoch", String.valueOf(epochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        String consoleMessage = Helper.replaceKeys(consoleConfigString, replacements);
        String discordMessage = Helper.replaceKeys(discordConfigString, replacements);
        String minecraftMessage = Helper.replaceKeys(minecraftConfigString, replacements);

        if (config.getAsBoolean(ConfigDataKey.LUCKPERMS_ENABLED)) {
            consoleMessage = replacePrefixSuffix(consoleMessage, playerUUID, aliasedTo, to);
            minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedTo, to);
            discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedTo, to);
        }

        // Log to Console
        if (config.getAsBoolean(ConfigDataKey.CONSOLE_SWITCH)) pluginLogger.accept(consoleMessage);

        // Log to Discord
        if (config.getAsBoolean(ConfigDataKey.DISCORD_SWITCH_ENABLED)) {
            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW);
            if (config.getAsBoolean(ConfigDataKey.DISCORD_SWITCH_USE_TIMESTAMP)) embedBuilder.setTimestamp(epochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_SWITCH_ENABLED))
            minecraftLogger.accept(minecraftMessage);
    }

    /**
     * Creates a sanitized {@link EmbedBuilder} based on the message.
     * @param playerUUID The {@link UUID} of the in-game player.
     * @param message The {@link String} message to send in the Discord server.
     * @return A sanitized {@link EmbedBuilder} containing the contents.
     */
    private EmbedBuilder simpleAuthorEmbedBuilder(UUID playerUUID, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(message, null, getPlayerHeadURL(playerUUID));
        return embedBuilder;
    }

    private String getPlayerHeadURL(UUID playerUUID) {
        return MINECRAFT_PLAYER_HEAD_URL.replace("{PLAYER_UUID}", playerUUID.toString());
    }

    public void sendFromDiscord(MessageReceivedEvent event) {
        String message = config.getAsString(ConfigDataKey.DISCORD_CHAT_MINECRAFT_MESSAGE);

        if (event.getMember() == null) return;

        String username = event.getMember().getUser().getName();
        String nickname = event.getMember().getNickname();

        if (nickname == null) nickname = username;

        String roleName = "[no-role]";
        Color roleColor = Color.GRAY;
        if (!event.getMember().getRoles().isEmpty()) {
            Role role = event.getMember().getRoles().get(0);
            roleName = role.getName();

            if (role.getColor() != null) roleColor = role.getColor();
        }

        String discordMessage = event.getMessage().getContentStripped();

        String hex = "#" + Integer.toHexString(roleColor.getRGB()).substring(2);

        message = Helper.replaceKeys(
                message,
                Tuple.of("role", String.format("<%s>%s</%s>", hex, roleName, hex)),
                Tuple.of("user", username),
                Tuple.of("nick", nickname),
                Tuple.of("message", discordMessage),
                Tuple.of("epoch", String.valueOf(epochHelper.getEpochSecond())),
                Tuple.of("time", getTimeString()),
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
        );

        if (config.getAsBoolean(ConfigDataKey.MINECRAFT_DISCORD_ENABLED)) globalLogger.accept(message);
    }

    private List<String> getPrefixBasedOnServerContext(User user, String... serverKeys) {
        return user.resolveInheritedNodes(QueryOptions.nonContextual())
                .stream()
                .filter((node) -> {
                    if (!node.getContexts().containsKey("server")) return true;
                    for (String key : serverKeys) if (node.getContexts().contains("server", key)) return true;
                    return false;
                })
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

    private List<String> getSuffixBasedOnServerContext(User user, String... serverKeys) {
        return user.resolveInheritedNodes(QueryOptions.nonContextual())
                .stream()
                .filter((node) -> {
                    if (!node.getContexts().containsKey("server")) return true;
                    for (String key : serverKeys) if (node.getContexts().contains("server", key)) return true;
                    return false;
                })
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
            List<String> prefixList = getPrefixBasedOnServerContext(user, serverName, aliasedServerName, "");
            List<String> suffixList = getSuffixBasedOnServerContext(user, serverName, aliasedServerName, "");

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

        long timeInMillis = epochHelper.getEpochMillisecond();
        DateTime time = new DateTime(timeInMillis).withZone(zone);

        return time.toString(format);
    }

}
