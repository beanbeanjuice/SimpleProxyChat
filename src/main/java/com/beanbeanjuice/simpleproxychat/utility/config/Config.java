package com.beanbeanjuice.simpleproxychat.utility.config;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Config {

    private YamlDocument yamlConfig;
    private final File configFolder;
    private final HashMap<ConfigDataKey, ConfigDataEntry> config;

    public Config(File configFolder) {
        this.configFolder = configFolder;
        config = new HashMap<>();
    }

    public void initialize() {
        try {
            yamlConfig = loadConfig();
            yamlConfig.update();
            yamlConfig.save();
            readConfig();
        } catch (IOException ignored) { }
    }

    public Object get(@NotNull ConfigDataKey key) {
        return config.get(key).data();
    }

    private void readConfig() throws IOException {
        config.put(ConfigDataKey.USE_DISCORD, new ConfigDataEntry(Boolean.valueOf(yamlConfig.getString("use_discord"))));
        config.put(ConfigDataKey.BOT_TOKEN, new ConfigDataEntry(yamlConfig.getString("BOT_TOKEN")));
        config.put(ConfigDataKey.CHANNEL_ID, new ConfigDataEntry(yamlConfig.getString("CHANNEL_ID")));

        config.put(ConfigDataKey.JOIN_FORMAT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("join-format"))));
        config.put(ConfigDataKey.LEAVE_FORMAT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("leave-format"))));
        config.put(ConfigDataKey.MESSAGE_FORMAT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("message-format"))));
        config.put(ConfigDataKey.SWITCH_FORMAT, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("switch-format"))));
        config.put(ConfigDataKey.SWITCH_FORMAT_NO_FROM, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("switch-format_NO_FROM"))));

        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN, new ConfigDataEntry(yamlConfig.getString("minecraft_to_discord_join")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE, new ConfigDataEntry(yamlConfig.getString("minecraft_to_discord_leave")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_SWITCH, new ConfigDataEntry(yamlConfig.getString("minecraft_to_discord_switch")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_MESSAGE, new ConfigDataEntry(yamlConfig.getString("minecraft_to_discord_message")));

        config.put(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE, new ConfigDataEntry(Helper.translateLegacyCodes(yamlConfig.getString("discord_to_minecraft_message"))));

        config.put(ConfigDataKey.PROXY_ENABLED_MESSAGE, new ConfigDataEntry(yamlConfig.getString("proxy_enabled")));
        config.put(ConfigDataKey.PROXY_DISABLED_MESSAGE, new ConfigDataEntry(yamlConfig.getString("proxy_disabled")));
        config.put(ConfigDataKey.PROXY_STATUS_TITLE, new ConfigDataEntry(yamlConfig.getString("proxy_status_title")));
        config.put(ConfigDataKey.PROXY_STATUS_MESSAGE, new ConfigDataEntry(yamlConfig.getString("proxy_status_message")));
        config.put(ConfigDataKey.PROXY_STATUS_ONLINE, new ConfigDataEntry(yamlConfig.getString("proxy_status_online")));
        config.put(ConfigDataKey.PROXY_STATUS_OFFLINE, new ConfigDataEntry(yamlConfig.getString("proxy_status_offline")));

        config.put(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(false));
    }

    private YamlDocument loadConfig() throws IOException {
        return YamlDocument.create(
                new File(configFolder, "config.yml"),
                Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
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
