package com.beanbeanjuice.simpleproxychat.shared.chat;

import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.discord.Bot;
import com.beanbeanjuice.simpleproxychat.shared.discord.DiscordChatHandler;
import com.beanbeanjuice.simpleproxychat.shared.socket.ChatMessageData;
import com.beanbeanjuice.simpleproxychat.shared.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.beanbeanjuice.simpleproxychat.shared.helper.EpochHelper;
import com.beanbeanjuice.simpleproxychat.shared.helper.LastMessagesHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.luckperms.api.LuckPerms;
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

    private final ISimpleProxyChat plugin;
    private final Config config;
    private final Bot discordBot;
    private final LastMessagesHelper lastMessagesHelper;

    public ChatHandler(ISimpleProxyChat plugin) {
        this.plugin = plugin;
        this.config = plugin.getSPCConfig();
        this.discordBot = plugin.getDiscordBot();
        this.lastMessagesHelper = new LastMessagesHelper(plugin.getSPCConfig());

        plugin.getDiscordBot().addRunnableToQueue(() -> plugin.getDiscordBot().getJDA().ifPresent((jda) -> jda.addEventListener(new DiscordChatHandler(config, this::sendFromDiscord))));
    }

    private Optional<String> getValidMessage(String message) {
        String messagePrefix = config.get(ConfigKey.PROXY_MESSAGE_PREFIX).asString();

        if (messagePrefix.isEmpty()) return Optional.of(message);
        if (!message.startsWith(messagePrefix)) return Optional.empty();

        message = message.substring(messagePrefix.length());
        if (message.isEmpty()) return Optional.empty();
        return Optional.of(message);
    }

    public void chat(ChatMessageData chatMessageData, String minecraftMessage, String discordMessage, String discordEmbedTitle, String discordEmbedMessage) {
        // Log to Console
        if (config.get(ConfigKey.CONSOLE_CHAT).asBoolean()) plugin.log(minecraftMessage);

        // Log to Discord
        if (config.get(ConfigKey.MINECRAFT_DISCORD_ENABLED).asBoolean()) {
            if (config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_USE).asBoolean()) {

                Color color = config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_COLOR).asColor();

                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(discordEmbedTitle, null, getPlayerHeadURL(chatMessageData.getPlayerUUID()))
                        .setDescription(discordEmbedMessage)
                        .setColor(color);

                if (config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP).asBoolean())
                    embedBuilder.setTimestamp(EpochHelper.getEpochInstant());

                discordBot.sendMessageEmbed(embedBuilder.build());
            } else {
                discordBot.sendMessage(discordMessage);
            }
        }

        // Log to Minecraft
        if (config.get(ConfigKey.MINECRAFT_CHAT_ENABLED).asBoolean()) {
            chatMessageData.chatSendToAllOtherPlayers(minecraftMessage);
            lastMessagesHelper.addMessage(minecraftMessage);
        }

    }

    public void runProxyChatMessage(ChatMessageData chatMessageData) {
        if (Helper.serverHasChatLocked(plugin, chatMessageData.getServername())) return;

        String playerMessage = chatMessageData.getMessage();
        String serverName = chatMessageData.getServername();
        String playerName = chatMessageData.getPlayerName();
        UUID playerUUID = chatMessageData.getPlayerUUID();

        Optional<String> optionalPlayerMessage = getValidMessage(playerMessage);
        if (optionalPlayerMessage.isEmpty()) return;
        playerMessage = optionalPlayerMessage.get();

        String minecraftConfigString = config.get(ConfigKey.MINECRAFT_CHAT_MESSAGE).asString();
        String discordConfigString = config.get(ConfigKey.MINECRAFT_DISCORD_MESSAGE).asString();

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("message", playerMessage));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("escaped_player", Helper.escapeString(playerName)));
        replacements.add(Tuple.of("epoch", String.valueOf(EpochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

        String minecraftMessage = CommonHelper.replaceKeys(minecraftConfigString, replacements);
        String discordMessage = CommonHelper.replaceKeys(discordConfigString, replacements);
        String discordEmbedTitle = CommonHelper.replaceKeys(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_TITLE).asString(), replacements);
        String discordEmbedMessage = CommonHelper.replaceKeys(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_MESSAGE).asString(), replacements);

        minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedServerName, serverName);
        discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);
        discordEmbedTitle = replacePrefixSuffix(discordEmbedTitle, chatMessageData.getPlayerUUID(), aliasedServerName, chatMessageData.getServername());

        if (config.get(ConfigKey.USE_HELPER).asBoolean()) {
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
        String configString = config.get(ConfigKey.MINECRAFT_LEAVE).asString();
        String discordConfigString = config.get(ConfigKey.DISCORD_LEAVE_MESSAGE).asString();

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("escaped_player", Helper.escapeString(playerName)));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("epoch", String.valueOf(EpochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

        String message = CommonHelper.replaceKeys(configString, replacements);
        String discordMessage = CommonHelper.replaceKeys(discordConfigString, replacements);

        message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
        discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);

        // Log to Console
        if (config.get(ConfigKey.CONSOLE_LEAVE).asBoolean()) plugin.log(message);

        // Log to Discord
        DISCORD_SENT: if (config.get(ConfigKey.DISCORD_LEAVE_ENABLED).asBoolean()) {
            if (!config.get(ConfigKey.DISCORD_LEAVE_USE_EMBED).asBoolean()) {
                discordBot.sendMessage(discordMessage);
                break DISCORD_SENT;
            }

            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.RED);
            if (config.get(ConfigKey.DISCORD_LEAVE_USE_TIMESTAMP).asBoolean()) embedBuilder.setTimestamp(EpochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.get(ConfigKey.MINECRAFT_LEAVE_ENABLED).asBoolean()) minecraftLogger.accept(message, Permission.READ_LEAVE_MESSAGE);
    }

    public void runProxyJoinMessage(String playerName, UUID playerUUID, String serverName,
                                    BiConsumer<String, Permission> minecraftLogger) {
        String configString = config.get(ConfigKey.MINECRAFT_JOIN).asString();
        String discordConfigString = config.get(ConfigKey.DISCORD_JOIN_MESSAGE).asString();

        String aliasedServerName = Helper.convertAlias(config, serverName);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("player", playerName));
        replacements.add(Tuple.of("escaped_player", Helper.escapeString(playerName)));
        replacements.add(Tuple.of("server", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.of("to", Helper.convertAlias(config, serverName)));
        replacements.add(Tuple.of("server", aliasedServerName));
        replacements.add(Tuple.of("original_server", serverName));
        replacements.add(Tuple.of("to", aliasedServerName));
        replacements.add(Tuple.of("original_to", serverName));
        replacements.add(Tuple.of("epoch", String.valueOf(EpochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

        String message = CommonHelper.replaceKeys(configString, replacements);
        String discordMessage = CommonHelper.replaceKeys(discordConfigString, replacements);

        message = replacePrefixSuffix(message, playerUUID, aliasedServerName, serverName);
        discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedServerName, serverName);

        // Log to Console
        if (config.get(ConfigKey.CONSOLE_JOIN).asBoolean()) plugin.log(message);

        // Log to Discord
        DISCORD_SENT: if (config.get(ConfigKey.DISCORD_JOIN_ENABLED).asBoolean()) {
            if (!config.get(ConfigKey.DISCORD_JOIN_USE_EMBED).asBoolean()) {
                discordBot.sendMessage(discordMessage);
                break DISCORD_SENT;
            }

            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.GREEN);
            if (config.get(ConfigKey.DISCORD_JOIN_USE_TIMESTAMP).asBoolean()) embedBuilder.setTimestamp(EpochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.get(ConfigKey.MINECRAFT_JOIN_ENABLED).asBoolean())
            minecraftLogger.accept(message, Permission.READ_JOIN_MESSAGE);
    }

    public void runProxySwitchMessage(String from, String to, String playerName, UUID playerUUID,
                                      Consumer<String> minecraftLogger, Consumer<String> playerLogger) {
        String consoleConfigString = config.get(ConfigKey.MINECRAFT_SWITCH_DEFAULT).asString();
        String discordConfigString = config.get(ConfigKey.DISCORD_SWITCH_MESSAGE).asString();
        String minecraftConfigString = config.get(ConfigKey.MINECRAFT_SWITCH_SHORT).asString();

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
        replacements.add(Tuple.of("escaped_player", Helper.escapeString(playerName)));
        replacements.add(Tuple.of("epoch", String.valueOf(EpochHelper.getEpochSecond())));
        replacements.add(Tuple.of("time", getTimeString()));
        replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

        String consoleMessage = CommonHelper.replaceKeys(consoleConfigString, replacements);
        String discordMessage = CommonHelper.replaceKeys(discordConfigString, replacements);
        String minecraftMessage = CommonHelper.replaceKeys(minecraftConfigString, replacements);

        consoleMessage = replacePrefixSuffix(consoleMessage, playerUUID, aliasedTo, to);
        minecraftMessage = replacePrefixSuffix(minecraftMessage, playerUUID, aliasedTo, to);
        discordMessage = replacePrefixSuffix(discordMessage, playerUUID, aliasedTo, to);

        // Log to Console
        if (config.get(ConfigKey.CONSOLE_SWITCH).asBoolean()) plugin.log(consoleMessage);

        // Log to Discord
        DISCORD_SENT: if (config.get(ConfigKey.DISCORD_SWITCH_ENABLED).asBoolean()) {
            if (!config.get(ConfigKey.DISCORD_SWITCH_USE_EMBED).asBoolean()) {
                discordBot.sendMessage(discordMessage);
                break DISCORD_SENT;
            }

            EmbedBuilder embedBuilder = simpleAuthorEmbedBuilder(playerUUID, discordMessage).setColor(Color.YELLOW);
            if (config.get(ConfigKey.DISCORD_SWITCH_USE_TIMESTAMP).asBoolean()) embedBuilder.setTimestamp(EpochHelper.getEpochInstant());
            discordBot.sendMessageEmbed(embedBuilder.build());
        }

        // Log to Minecraft
        if (config.get(ConfigKey.MINECRAFT_SWITCH_ENABLED).asBoolean()) {
            minecraftLogger.accept(minecraftMessage);
            lastMessagesHelper.getBoundedArrayList().forEach(playerLogger);
        }
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
        String message = config.get(ConfigKey.DISCORD_CHAT_MINECRAFT_MESSAGE).asString();

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

        message = CommonHelper.replaceKeys(
                message,
                Tuple.of("role", String.format("<%s>%s</%s>", hex, roleName, hex)),
                Tuple.of("user", username),
                Tuple.of("nick", nickname),
                Tuple.of("message", discordMessage),
                Tuple.of("epoch", String.valueOf(EpochHelper.getEpochSecond())),
                Tuple.of("time", getTimeString()),
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
        );

        if (config.get(ConfigKey.MINECRAFT_DISCORD_ENABLED).asBoolean()) plugin.sendAll(message);
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
        if (!this.plugin.isLuckPermsEnabled()) return message;

        return this.plugin.getLuckPerms().map(LuckPerms.class::cast).map((luckPerms) -> {
            User user = null;
            try {
                user = luckPerms.getUserManager().loadUser(playerUUID).get();
            } catch (Exception e) {
                plugin.log("Error contacting the LuckPerms API: " + e.getMessage());
                return message;
            }

            // Get prefix based on aliased name. If none show up, use original name. If none show up, use top prefix.
            List<String> prefixList = getPrefixBasedOnServerContext(user, serverName, aliasedServerName, "");
            List<String> suffixList = getSuffixBasedOnServerContext(user, serverName, aliasedServerName, "");

            String prefix = prefixList.isEmpty() ? "" : CommonHelper.translateLegacyCodes(prefixList.get(0));
            String suffix = suffixList.isEmpty() ? "" : CommonHelper.translateLegacyCodes(suffixList.get(0));

            return message.replace("%prefix%", prefix).replace("%suffix%", suffix);
        }).orElse(message);
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html">Format</a>
     */
    private String getTimeString() {
        DateTimeZone zone = config.get(ConfigKey.TIMESTAMP_TIMEZONE).asDateTimeZone();
        DateTimeFormatter format = DateTimeFormat.forPattern(config.get(ConfigKey.TIMESTAMP_FORMAT).asString());

        long timeInMillis = EpochHelper.getEpochMillisecond();
        DateTime time = new DateTime(timeInMillis).withZone(zone);

        return time.toString(format);
    }

}
