package com.plyblox.proxychat.utility.config;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.Helper;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class Config {

    private final ProxyChat plugin;
    private final HashMap<ConfigDataKey, ConfigDataEntry> config;

    public Config(ProxyChat plugin) {
        this.plugin = plugin;
        config = new HashMap<>();
    }

    public void initialize() {
        try {
            makeConfig();
            makeConfigAlternative();
            populateConfig();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[ProxyChat] Unable to create config...");
        }
    }

    public Object get(@NotNull ConfigDataKey key) {
        return config.get(key).getData();
    }

    private void populateConfig() throws IOException {
        Configuration configurationFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));
        config.put(ConfigDataKey.BOT_TOKEN, new ConfigDataEntry(configurationFile.getString("BOT_TOKEN")));
        config.put(ConfigDataKey.CHANNEL_ID, new ConfigDataEntry(configurationFile.getString("CHANNEL_ID")));

        config.put(ConfigDataKey.PREFIX, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("prefix"))));

        config.put(ConfigDataKey.JOIN_FORMAT, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("join-format"))));
        config.put(ConfigDataKey.LEAVE_FORMAT, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("leave-format"))));
        config.put(ConfigDataKey.MESSAGE_FORMAT, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("message-format"))));
        config.put(ConfigDataKey.SWITCH_FORMAT, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("switch-format"))));
        config.put(ConfigDataKey.SWITCH_FORMAT_NO_FROM, new ConfigDataEntry(Helper.translateColors(configurationFile.getString("switch-format_NO_FROM"))));

        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_JOIN, new ConfigDataEntry(configurationFile.getString("minecraft_to_discord_join")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_LEAVE, new ConfigDataEntry(configurationFile.getString("minecraft_to_discord_leave")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_SWITCH, new ConfigDataEntry(configurationFile.getString("minecraft_to_discord_switch")));
        config.put(ConfigDataKey.MINECRAFT_TO_DISCORD_MESSAGE, new ConfigDataEntry(configurationFile.getString("minecraft_to_discord_message")));

        config.put(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE, new ConfigDataEntry(configurationFile.getString("discord_to_minecraft_message")));
    }

    private void makeConfig() throws IOException {
        // Create plugin config folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getLogger().info("Created config folder: " + plugin.getDataFolder().mkdir());
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
            InputStream in = plugin.getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
            in.transferTo(outputStream); // Throws IOException
        }
    }


    // Doesnt work for some later java versions
    private void makeConfigAlternative() throws IOException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
