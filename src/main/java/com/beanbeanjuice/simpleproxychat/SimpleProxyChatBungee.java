package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.chat.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.chat.BungeeVanishListener;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
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

public final class SimpleProxyChatBungee extends Plugin {

    @Getter
    private Config config;

    @Getter
    private Bot discordBot;

    @Override
    public void onEnable() {
        this.getLogger().info("The plugin is starting.");

        this.config = new Config(this.getDataFolder());
        this.config.initialize();

        PluginManager pm = this.getProxy().getPluginManager();

        this.getLogger().info("Initializing discord bot.");

        try {
            discordBot = new Bot((String) this.config.get(ConfigDataKey.BOT_TOKEN), this.config);
        }
        catch (InterruptedException e) { throw new RuntimeException(e); }

        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle("✅ Proxy enabled!")
                        .setColor(Color.GREEN)
                        .build()
        );

        // Registering Chat Listener
        ChatHandler chatHandler = new ChatHandler(config, discordBot, (message) -> {
            Component minimessage = MiniMessage.miniMessage().deserialize(message);
            this.getProxy().broadcast(BungeeComponentSerializer.get().serialize(minimessage));
        });

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
                        .setTitle("⛔ Proxy disabled.")
                        .setColor(Color.RED)
                        .build()
        );

        discordBot.updateChannelTopic("The proxy is offline.");
    }

}
