package com.beanbeanjuice.simpleproxychat.utility.config;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.ServerChatLockHelper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class Config {

    private YamlDocument yamlConfig;
    private YamlDocument yamlMessages;
    private final File configFolder;
    private final HashMap<ConfigDataKey, Object> config;

    private boolean initialSetup = true;
    @Getter private final ServerChatLockHelper serverChatLockHelper;

    public Config(File configFolder) {
        this.configFolder = configFolder;
        config = new HashMap<>();
        serverChatLockHelper = new ServerChatLockHelper();
    }

    public void initialize() {
        try {
            yamlConfig = loadConfig("config.yml");
            yamlMessages = loadConfig("messages.yml");
            yamlConfig.update();
            yamlMessages.update();
            yamlConfig.save();
            yamlMessages.save();
            readConfig();
        } catch (IOException ignored) { }
    }

    public void reload() {
        try {
            yamlConfig.reload();
            yamlMessages.reload();
            readConfig();
        } catch (IOException ignored) { }
    }

    private Object get(ConfigDataKey key) {
        return config.get(key);
    }

    public String getAsString(ConfigDataKey key) {
        return (String) get(key);
    }

    public int getAsInteger(ConfigDataKey key) {
        return (int) get(key);
    }

    public boolean getAsBoolean(ConfigDataKey key) {
        return (boolean) get(key);
    }

    public Optional<Color> getAsColor(ConfigDataKey key) {
        try {
            return Optional.of(Color.decode(getAsString(key)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> getAsStringMap(ConfigDataKey key) {
        return (HashMap<String, String>) get(key);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getAsArrayList(ConfigDataKey key) {
        return (ArrayList<String>) get(key);
    }

    private void readConfig() throws IOException {
        // config.yml
        config.put(ConfigDataKey.USE_DISCORD, Boolean.valueOf(yamlConfig.getString("use-discord")));
        config.put(ConfigDataKey.BOT_TOKEN, yamlConfig.getString("BOT-TOKEN"));
        config.put(ConfigDataKey.CHANNEL_ID, yamlConfig.getString("CHANNEL-ID"));
        config.put(ConfigDataKey.BOT_ACTIVITY_TYPE, yamlConfig.getString("bot-activity.type"));
        config.put(ConfigDataKey.BOT_ACTIVITY_TEXT, yamlConfig.getString("bot-activity.text"));
        config.put(ConfigDataKey.SERVER_UPDATE_INTERVAL, yamlConfig.getInt("server-update-interval"));
        HashMap<String, String> aliases = new HashMap<>();
        Section aliasSection = yamlConfig.getSection("aliases");
        aliasSection.getKeys().stream()
                .map((key) -> (String) key)
                .forEach((key) -> aliases.put(key, aliasSection.getString(key)));
        config.put(ConfigDataKey.ALIASES, aliases);
        config.put(ConfigDataKey.USE_PERMISSIONS, yamlConfig.getBoolean("use-permissions"));
        config.put(ConfigDataKey.PROXY_MESSAGE_PREFIX, yamlConfig.getString("proxy-message-prefix"));
        config.put(ConfigDataKey.USE_INITIAL_SERVER_STATUS, yamlConfig.getBoolean("use-initial-server-status"));
        config.put(ConfigDataKey.USE_FAKE_MESSAGES, yamlConfig.getBoolean("use-fake-messages"));
        config.put(ConfigDataKey.TIMESTAMP_USE_API, yamlConfig.getBoolean("timestamp.use-api"));
        config.put(ConfigDataKey.TIMESTAMP_FORMAT, yamlConfig.getString("timestamp.format"));
        config.put(ConfigDataKey.TIMESTAMP_TIMEZONE, yamlConfig.getString("timestamp.timezone"));
        config.put(ConfigDataKey.USE_HELPER, Boolean.valueOf(yamlConfig.getString("use-helper")));
        config.put(ConfigDataKey.UPDATE_NOTIFICATIONS, Boolean.valueOf(yamlConfig.getString("update-notifications")));

        ArrayList<String> whisperAliases = (ArrayList<String>) yamlConfig.getStringList("commands.whisper-aliases");
        config.put(ConfigDataKey.WHISPER_ALIASES, whisperAliases);
        ArrayList<String> replyAliases = (ArrayList<String>) yamlConfig.getStringList("commands.reply-aliases");
        config.put(ConfigDataKey.REPLY_ALIASES, replyAliases);

        // Checking timezone.
        try {
            DateTimeZone.forID(getAsString(ConfigDataKey.TIMESTAMP_TIMEZONE));
        } catch (IllegalArgumentException e) {
            System.err.printf(
                    "%s is not a valid timezone. Using default timezone. %s%n",
                    getAsString(ConfigDataKey.TIMESTAMP_TIMEZONE),
                    "https://www.joda.org/joda-time/timezones.html"
            );
            overwrite(ConfigDataKey.TIMESTAMP_TIMEZONE, "America/Los_Angeles");
        }

        // messages.yml
        putMessage(ConfigDataKey.PLUGIN_PREFIX, "plugin-prefix", false);

        config.put(ConfigDataKey.MINECRAFT_JOIN_ENABLED, yamlMessages.getBoolean("minecraft.join.enabled"));
        putMessage(ConfigDataKey.MINECRAFT_JOIN, "minecraft.join.message", false);
        config.put(ConfigDataKey.MINECRAFT_LEAVE_ENABLED, yamlMessages.getBoolean("minecraft.leave.enabled"));
        putMessage(ConfigDataKey.MINECRAFT_LEAVE, "minecraft.leave.message", false);
        config.put(ConfigDataKey.MINECRAFT_CHAT_ENABLED, yamlMessages.getBoolean("minecraft.chat.enabled"));
        putMessage(ConfigDataKey.MINECRAFT_CHAT_MESSAGE, "minecraft.chat.message", false);
        config.put(ConfigDataKey.MINECRAFT_SWITCH_ENABLED, yamlMessages.getBoolean("minecraft.switch.enabled"));
        putMessage(ConfigDataKey.MINECRAFT_SWITCH_DEFAULT, "minecraft.switch.default", false);
        putMessage(ConfigDataKey.MINECRAFT_SWITCH_SHORT, "minecraft.switch.no-from", false);
        putMessage(ConfigDataKey.MINECRAFT_WHISPER_SEND, "minecraft.whisper.send", false);
        putMessage(ConfigDataKey.MINECRAFT_WHISPER_RECEIVE, "minecraft.whisper.receive", false);
        putMessage(ConfigDataKey.MINECRAFT_WHISPER_ERROR, "minecraft.whisper.error", false);
        config.put(ConfigDataKey.MINECRAFT_DISCORD_ENABLED, yamlMessages.getBoolean("minecraft.discord.enabled"));
        putMessage(ConfigDataKey.MINECRAFT_DISCORD_MESSAGE, "minecraft.discord.message", true);
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE, yamlMessages.getBoolean("minecraft.discord.embed.use"));
        putMessage(ConfigDataKey.MINECRAFT_DISCORD_EMBED_TITLE, "minecraft.discord.embed.title", true);
        putMessage(ConfigDataKey.MINECRAFT_DISCORD_EMBED_MESSAGE, "minecraft.discord.embed.message", true);
        putMessage(ConfigDataKey.MINECRAFT_DISCORD_EMBED_COLOR, "minecraft.discord.embed.color", true);
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP, yamlMessages.getBoolean("minecraft.discord.embed.use-timestamp"));
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_NO_PERMISSION, "minecraft.command.no-permission", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_UNKNOWN, "minecraft.command.unknown", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_MUST_BE_PLAYER, "minecraft.command.must-be-player", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_RELOAD, "minecraft.command.reload", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_USAGE, "minecraft.command.chat-lock.usage", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_LOCKED, "minecraft.command.chat-lock.single.locked", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_UNLOCKED, "minecraft.command.chat-lock.single.unlocked", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_LOCKED, "minecraft.command.chat-lock.all.locked", false);
        putMessage(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_UNLOCKED, "minecraft.command.chat-lock.all.unlocked", false);

        config.put(ConfigDataKey.DISCORD_JOIN_ENABLED, yamlMessages.getBoolean("discord.join.enabled"));
        putMessage(ConfigDataKey.DISCORD_JOIN_MESSAGE, "discord.join.message", true);
        config.put(ConfigDataKey.DISCORD_JOIN_USE_TIMESTAMP, yamlMessages.getBoolean("discord.join.use-timestamp"));
        config.put(ConfigDataKey.DISCORD_LEAVE_ENABLED, yamlMessages.getBoolean("discord.leave.enabled"));
        putMessage(ConfigDataKey.DISCORD_LEAVE_MESSAGE, "discord.leave.message", true);
        config.put(ConfigDataKey.DISCORD_LEAVE_USE_TIMESTAMP, yamlMessages.getBoolean("discord.leave.use-timestamp"));
        config.put(ConfigDataKey.DISCORD_SWITCH_ENABLED, yamlMessages.getBoolean("discord.switch.enabled"));
        putMessage(ConfigDataKey.DISCORD_SWITCH_MESSAGE, "discord.switch.message", true);
        config.put(ConfigDataKey.DISCORD_SWITCH_USE_TIMESTAMP, yamlMessages.getBoolean("discord.switch.use-timestamp"));
        config.put(ConfigDataKey.DISCORD_CHAT_ENABLED, yamlMessages.getBoolean("discord.chat.enabled"));
        putMessage(ConfigDataKey.DISCORD_CHAT_MINECRAFT_MESSAGE, "discord.chat.minecraft-message", false);
        putMessage(ConfigDataKey.DISCORD_TOPIC_ONLINE, "discord.topic.online", true);
        putMessage(ConfigDataKey.DISCORD_TOPIC_OFFLINE, "discord.topic.offline", true);
        config.put(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED, yamlMessages.getBoolean("discord.proxy-status.enabled"));
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_ENABLED, "discord.proxy-status.messages.enabled", true);
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_DISABLED, "discord.proxy-status.messages.disabled", true);
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_TITLE, "discord.proxy-status.messages.title", true);
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_MESSAGE, "discord.proxy-status.messages.message", true);
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_ONLINE, "discord.proxy-status.messages.online", true);
        putMessage(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_OFFLINE, "discord.proxy-status.messages.offline", true);
        config.put(ConfigDataKey.DISCORD_PROXY_STATUS_MODULE_USE_TIMESTAMP, yamlMessages.getBoolean("discord.proxy-status.messages.use-timestamp"));
        putMessage(ConfigDataKey.UPDATE_MESSAGE, "update-message", false);

        config.put(ConfigDataKey.CONSOLE_CHAT, yamlMessages.getBoolean("console.chat"));
        config.put(ConfigDataKey.CONSOLE_JOIN, yamlMessages.getBoolean("console.join"));
        config.put(ConfigDataKey.CONSOLE_LEAVE, yamlMessages.getBoolean("console.leave"));
        config.put(ConfigDataKey.CONSOLE_SWITCH, yamlMessages.getBoolean("console.switch"));
        config.put(ConfigDataKey.CONSOLE_DISCORD_CHAT, yamlMessages.getBoolean("console.discord-chat"));
        config.put(ConfigDataKey.CONSOLE_SERVER_STATUS, yamlMessages.getBoolean("console.server-status"));

        // External
        if (!initialSetup) return;
        config.put(ConfigDataKey.VANISH_ENABLED, false);
        config.put(ConfigDataKey.LUCKPERMS_ENABLED, false);
        config.put(ConfigDataKey.LITEBANS_ENABLED, false);
        config.put(ConfigDataKey.ADVANCEDBAN_ENABLED, false);
        config.put(ConfigDataKey.NETWORKMANAGER_ENABLED, false);
        config.put(ConfigDataKey.PLUGIN_STARTING, true);
        initialSetup = false;
    }

    private void putMessage(ConfigDataKey type, String path, boolean isDiscord) {
        config.put(type, (isDiscord) ? yamlMessages.getString(path) : Helper.translateLegacyCodes(yamlMessages.getString(path)));
    }

    private YamlDocument loadConfig(String fileName) throws IOException {
        return YamlDocument.create(
                new File(configFolder, fileName),
                Objects.requireNonNull(getClass().getResourceAsStream("/" + fileName)),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning("file-version"))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)

                        .addRelocation("7", "minecraft.join.use", "minecraft.join.enabled", '.')
                        .addRelocation("7", "minecraft.leave.use", "minecraft.leave.enabled", '.')
                        .addRelocation("7", "minecraft.message", "minecraft.chat.message", '.')
                        .addRelocation("7", "minecraft.switch.use", "minecraft.switch.enabled", '.')
                        .addRelocation("7", "discord.join.use", "discord.join.enabled", '.')
                        .addRelocation("7", "discord.leave.use", "discord.leave.enabled", '.')
                        .addRelocation("7", "discord.switch.use", "discord.switch.enabled", '.')
                        .addRelocation("7", "discord.minecraft-message", "discord.chat.minecraft-message", '.')

                        .addRelocation("7", "discord.proxy-status.enabled", "discord.proxy-status.messages.enabled", '.')
                        .addRelocation("7", "discord.proxy-status.disabled", "discord.proxy-status.messages.disabled", '.')
                        .addRelocation("7", "discord.proxy-status.title", "discord.proxy-status.messages.title", '.')
                        .addRelocation("7", "discord.proxy-status.message", "discord.proxy-status.messages.message", '.')
                        .addRelocation("7", "discord.proxy-status.online", "discord.proxy-status.messages.online", '.')
                        .addRelocation("7", "discord.proxy-status.offline", "discord.proxy-status.messages.offline", '.')
                        .addRelocation("7", "discord.proxy-status.use-timestamp", "discord.proxy-status.messages.use-timestamp", '.')

                        .build()
        );
    }

    public void overwrite(ConfigDataKey key, Object entry) {
        config.put(key, entry);
    }

}
