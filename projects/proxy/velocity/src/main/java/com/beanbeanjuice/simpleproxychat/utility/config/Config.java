package com.beanbeanjuice.simpleproxychat.utility.config;

import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.helper.ServerChatLockHelper;
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
import java.util.*;
import java.util.List;

public class Config {

    private YamlDocument yamlConfig;
    private YamlDocument yamlMessages;
    private final File configFolder;
    private final HashMap<ConfigKey, ConfigValueWrapper> config;
    private final ArrayList<Runnable> reloadFunctions;

    @Getter private final ServerChatLockHelper serverChatLockHelper;

    public Config(File configFolder) {
        this.configFolder = configFolder;
        config = new HashMap<>();
        reloadFunctions = new ArrayList<>();
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

    public void addReloadListener(Runnable runnable) {
        reloadFunctions.add(runnable);
    }

    public void reload() {
        try {
            yamlConfig.reload();
            yamlMessages.reload();
            readConfig();
            reloadFunctions.forEach(Runnable::run);
        } catch (IOException ignored) { }
    }

    public ConfigValueWrapper get(ConfigKey key) {
        return config.get(key);
    }

    private void readConfig() throws IOException {
        Arrays.stream(ConfigKey.values()).forEach((key) -> {
            YamlDocument document = (key.getFile() == ConfigFileType.CONFIG) ? yamlConfig : yamlMessages;
            String route = key.getKey();

            if (key.getClassType() == String.class) {
                String message = CommonHelper.translateLegacyCodes(document.getString(route));
                this.config.put(key, new ConfigValueWrapper(message));
                return;
            }

            if (key.getClassType() == Integer.class) {
                this.config.put(key, new ConfigValueWrapper(document.getInt(route)));
                return;
            }

            if (key.getClassType() == Boolean.class) {
                this.config.put(key, new ConfigValueWrapper(document.getBoolean(route)));
                return;
            }

            if (key.getClassType() == Map.class) {
                Map<String, String> map = new HashMap<>();
                Section mapSection = document.getSection(route);
                mapSection.getKeys().stream()
                        .map((mapKey) -> (String) mapKey)
                        .map((mapKey) -> Tuple.of(mapKey, mapSection.getString(mapKey)))
                        .forEach((pair) -> map.put(pair.getKey(), CommonHelper.translateLegacyCodes(pair.getValue())));

                this.config.put(key, new ConfigValueWrapper(map));
                return;
            }

            if (key.getClassType() == List.class) {
                List<String> list = document.getStringList(route);
                this.config.put(key, new ConfigValueWrapper(list.stream().map(CommonHelper::translateLegacyCodes).toList()));
                return;
            }

            if (key.getClassType() == Color.class) {
                String colorString = document.getString(route);
                Color color;

                try {
                    color = Color.decode(colorString);
                } catch (NumberFormatException e) {
                    System.err.printf(
                            "%s is not a valid color. Defaulting to black.\n",
                            colorString
                    );
                    color = Color.black;
                }

                this.config.put(key, new ConfigValueWrapper(color));
                return;
            }

            if (key.getClassType() == DateTimeZone.class) {
                String timezoneString = document.getString(route);
                DateTimeZone timezone;

                try {
                    timezone = DateTimeZone.forID(timezoneString);
                } catch (IllegalArgumentException e) {
                    System.err.printf(
                            "%s is not a valid timezone. Using default timezone. %s\n",
                            timezoneString, "https://www.joda.org/joda-time/timezones.html"
                    );
                    timezone = DateTimeZone.forID("America/Los_Angeles");
                }

                this.config.put(key, new ConfigValueWrapper(timezone));
                return;
            }

        });

    }

    public void overwrite(ConfigKey key, Object value) {
        config.put(key, new ConfigValueWrapper(value));
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

}
