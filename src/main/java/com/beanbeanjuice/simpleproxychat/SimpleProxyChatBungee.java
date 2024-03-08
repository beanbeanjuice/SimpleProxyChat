package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeVanishListener;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataEntry;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.MultiLineChart;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public final class SimpleProxyChatBungee extends Plugin {

    private Config config;
    private Bot discordBot;

    @Override
    public void onEnable() {
        this.getLogger().info("The plugin is starting.");

        this.config = new Config(this.getDataFolder());
        this.config.initialize();

        PluginManager pm = this.getProxy().getPluginManager();

        this.getLogger().info("Initializing discord bot.");

        try { discordBot = new Bot(this.config); }
        catch (Exception e) { getLogger().warning("There was an error starting the discord bot: " + e.getMessage()); }

        discordBot.getJDA().ifPresentOrElse((jda) -> { }, () -> getLogger().warning("Discord logging is not enabled."));

        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle((String) config.get(ConfigDataKey.PROXY_ENABLED_MESSAGE))
                        .setColor(Color.GREEN)
                        .build()
        );

        // Registering LuckPerms support.
        if (pm.getPlugin("LuckPerms") != null) {
            try {
                config.overwrite(ConfigDataKey.LUCKPERMS_ENABLED, new ConfigDataEntry(true));
                getLogger().info("LuckPerms support has been enabled.");
            } catch (IllegalStateException e) {
                getLogger().info("Error Enabling LuckPerms: " + e.getMessage());
            }
        }

        // Registering Chat Listener
        ChatHandler chatHandler = new ChatHandler(
                config,
                discordBot,
                (message) -> {
                    Component minimessage = MiniMessage.miniMessage().deserialize(message);
                    this.getProxy().broadcast(BungeeComponentSerializer.get().serialize(minimessage));
                },
                (message) -> getLogger().info(message)
        );

        BungeeServerListener serverListener = new BungeeServerListener(this, chatHandler);
        this.getProxy().getPluginManager().registerListener(this, serverListener);

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish") != null || pm.getPlugin("SuperVanish") != null) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.getLogger().log(Level.INFO, "Enabled PremiumVanish/SuperVanish Support");
            this.getProxy().getPluginManager().registerListener(this, new BungeeVanishListener(serverListener));
        }

        // Discord Topic Updater
        this.getProxy().getScheduler().schedule(this, () -> {
            discordBot.channelUpdaterFunction(this.getProxy().getPlayers().size());
        }, 5, 5, TimeUnit.MINUTES);

        // Update Checker
        this.getProxy().getScheduler().schedule(this, () -> UpdateChecker.checkUpdate((spigotMCVersion) -> {
            if (!this.getDescription().getVersion().equals(spigotMCVersion)) {
                this.getLogger().info("ATTENTION - There is a new update available: v" + spigotMCVersion);
            }
        }), 0, 2, TimeUnit.HOURS);

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        int pluginId = 21146;
        Metrics metrics = new Metrics(this, pluginId);

        // Optional: Add custom charts
        metrics.addCustomChart(new MultiLineChart("players_and_servers", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            valueMap.put("servers", 1);
            valueMap.put("players", this.getProxy().getOnlineCount());
            return valueMap;
        }));

        this.getLogger().log(Level.INFO, "The plugin has been started.");
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "The plugin is shutting down.");
        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle((String) config.get(ConfigDataKey.PROXY_DISABLED_MESSAGE))
                        .setColor(Color.RED)
                        .build()
        );

        discordBot.updateChannelTopic("The proxy is offline.");
    }

}
