package com.beanbeanjuice.simpleproxychat.utility.config;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class Config {

    private YamlDocument yamlConfig;
    private YamlDocument yamlMessages;
    private final File configFolder;
    private final HashMap<ConfigDataKey, ConfigDataEntry> config;

    private boolean initialSetup = true;

    public Config(File configFolder) {
        this.configFolder = configFolder;
        config = new HashMap<>();
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
        return config.get(key).data();
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

    private void readConfig() throws IOException {
        // config.yml
        config.put(ConfigDataKey.USE_DISCORD, new ConfigDataEntry(Boolean.valueOf(yamlConfig.getString("use-discord"))));
        config.put(ConfigDataKey.BOT_TOKEN, new ConfigDataEntry(yamlConfig.getString("BOT-TOKEN")));
        config.put(ConfigDataKey.CHANNEL_ID, new ConfigDataEntry(yamlConfig.getString("CHANNEL-ID")));
        config.put(ConfigDataKey.SERVER_UPDATE_INTERVAL, new ConfigDataEntry(yamlConfig.getInt("server-update-interval")));
        HashMap<String, String> aliases = new HashMap<>();
        Section aliasSection = yamlConfig.getSection("aliases");
        aliasSection.getKeys().stream()
                .map((key) -> (String) key)
                .forEach((key) -> aliases.put(key, aliasSection.getString(key)));
        config.put(ConfigDataKey.ALIASES, new ConfigDataEntry(aliases));
        config.put(ConfigDataKey.USE_PERMISSIONS, new ConfigDataEntry(yamlConfig.getBoolean("use-permissions")));
        config.put(ConfigDataKey.USE_INITIAL_SERVER_STATUS, new ConfigDataEntry(yamlConfig.getBoolean("use-initial-server-status")));
        config.put(ConfigDataKey.TIMESTAMP_FORMAT, new ConfigDataEntry(yamlConfig.getString("timestamp.format")));
        config.put(ConfigDataKey.TIMESTAMP_TIMEZONE, new ConfigDataEntry(yamlConfig.getString("timestamp.timezone")));

        // Checking timezone.
        try {
            DateTimeZone.forID(getAsString(ConfigDataKey.TIMESTAMP_TIMEZONE));
        } catch (IllegalArgumentException e) {
            System.err.printf(
                    "%s is not a valid timezone. Using default timezone. %s%n",
                    getAsString(ConfigDataKey.TIMESTAMP_TIMEZONE),
                    "https://www.joda.org/joda-time/timezones.html"
            );
            overwrite(ConfigDataKey.TIMESTAMP_TIMEZONE, new ConfigDataEntry("America/Los_Angeles"));
        }

        // messages.yml
        config.put(ConfigDataKey.MINECRAFT_JOIN_USE, new ConfigDataEntry(yamlMessages.getBoolean("minecraft.join.use")));
        config.put(ConfigDataKey.MINECRAFT_JOIN, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.join.message"))));
        config.put(ConfigDataKey.MINECRAFT_LEAVE_USE, new ConfigDataEntry(yamlMessages.getBoolean("minecraft.leave.use")));
        config.put(ConfigDataKey.MINECRAFT_LEAVE, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.leave.message"))));
        config.put(ConfigDataKey.MINECRAFT_MESSAGE, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.message"))));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_MESSAGE, new ConfigDataEntry(yamlMessages.getString("minecraft.discord.message")));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE, new ConfigDataEntry(yamlMessages.getBoolean("minecraft.discord.embed.use")));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_TITLE, new ConfigDataEntry(yamlMessages.getString("minecraft.discord.embed.title")));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_MESSAGE, new ConfigDataEntry(yamlMessages.getString("minecraft.discord.embed.message")));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_COLOR, new ConfigDataEntry(yamlMessages.getString("minecraft.discord.embed.color")));
        config.put(ConfigDataKey.MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP, new ConfigDataEntry(yamlMessages.getBoolean("minecraft.discord.embed.use-timestamp")));
        config.put(ConfigDataKey.MINECRAFT_SWITCH_USE, new ConfigDataEntry(yamlMessages.getBoolean("minecraft.switch.use")));
        config.put(ConfigDataKey.MINECRAFT_SWITCH_DEFAULT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.switch.default"))));
        config.put(ConfigDataKey.MINECRAFT_SWITCH_SHORT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.switch.no-from"))));
        config.put(ConfigDataKey.MINECRAFT_SUCCESSFUL_RELOAD, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.successful-reload"))));
        config.put(ConfigDataKey.MINECRAFT_NO_PERMISSION, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("minecraft.no-permission"))));

        config.put(ConfigDataKey.DISCORD_JOIN_USE, new ConfigDataEntry(yamlMessages.getBoolean("discord.join.use")));
        config.put(ConfigDataKey.DISCORD_JOIN_MESSAGE, new ConfigDataEntry(yamlMessages.getString("discord.join.message")));
        config.put(ConfigDataKey.DISCORD_JOIN_USE_TIMESTAMP, new ConfigDataEntry(yamlMessages.getBoolean("discord.join.use-timestamp")));
        config.put(ConfigDataKey.DISCORD_LEAVE_USE, new ConfigDataEntry(yamlMessages.getBoolean("discord.leave.use")));
        config.put(ConfigDataKey.DISCORD_LEAVE_MESSAGE, new ConfigDataEntry(yamlMessages.getString("discord.leave.message")));
        config.put(ConfigDataKey.DISCORD_LEAVE_USE_TIMESTAMP, new ConfigDataEntry(yamlMessages.getBoolean("discord.leave.use-timestamp")));
        config.put(ConfigDataKey.DISCORD_SWITCH_USE, new ConfigDataEntry(yamlMessages.getBoolean("discord.switch.use")));
        config.put(ConfigDataKey.DISCORD_SWITCH_MESSAGE, new ConfigDataEntry(yamlMessages.getString("discord.switch.message")));
        config.put(ConfigDataKey.DISCORD_SWITCH_USE_TIMESTAMP, new ConfigDataEntry(yamlMessages.getBoolean("discord.switch.use-timestamp")));
        config.put(ConfigDataKey.DISCORD_MINECRAFT_MESSAGE, new ConfigDataEntry(Helper.translateLegacyCodes(yamlMessages.getString("discord.minecraft-message"))));
        config.put(ConfigDataKey.DISCORD_PROXY_ENABLED, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.enabled")));
        config.put(ConfigDataKey.DISCORD_PROXY_DISABLED, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.disabled")));
        config.put(ConfigDataKey.DISCORD_PROXY_TITLE, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.title")));
        config.put(ConfigDataKey.DISCORD_PROXY_MESSAGE, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.message")));
        config.put(ConfigDataKey.DISCORD_PROXY_STATUS_ONLINE, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.online")));
        config.put(ConfigDataKey.DISCORD_PROXY_STATUS_OFFLINE, new ConfigDataEntry(yamlMessages.getString("discord.proxy-status.offline")));
        config.put(ConfigDataKey.DISCORD_PROXY_STATUS_USE_TIMESTAMP, new ConfigDataEntry(yamlMessages.getBoolean("discord.proxy-status.use-timestamp")));

        // External
        if (!initialSetup) return;
        config.put(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(false));
        config.put(ConfigDataKey.LUCKPERMS_ENABLED, new ConfigDataEntry(false));
        config.put(ConfigDataKey.PLUGIN_STARTING, new ConfigDataEntry(true));
        initialSetup = false;
    }

    private YamlDocument loadConfig(String fileName) throws IOException {
        return YamlDocument.create(
                new File(configFolder, fileName),
                Objects.requireNonNull(getClass().getResourceAsStream("/" + fileName)),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
        );
    }

    public void overwrite(ConfigDataKey key, ConfigDataEntry entry) {
        config.put(key, entry);
    }

}
