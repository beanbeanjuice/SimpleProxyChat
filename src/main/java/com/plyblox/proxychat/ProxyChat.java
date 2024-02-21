package com.plyblox.proxychat;

import com.plyblox.proxychat.chat.ServerChatHandler;
import com.plyblox.proxychat.discord.Bot;
import com.plyblox.proxychat.utility.config.Config;
import com.plyblox.proxychat.utility.config.ConfigDataEntry;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.GroupedThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.logging.Level;

public final class ProxyChat extends Plugin {

    private Config config;
    private Bot discordBot;

    @Override
    public void onEnable() {
        this.getLogger().log(Level.INFO, "The plugin is starting.");

        this.config = new Config(this);
        this.config.initialize();

        PluginManager pm = this.getProxy().getPluginManager();

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish") != null || pm.getPlugin("SuperVanish") != null) {
            config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.getLogger().log(Level.INFO, "Enabled PremiumVanish/SuperVanish Support");
        }

        this.getLogger().log(Level.INFO, "Initializing discord bot.");

        try { discordBot = new Bot((String) config.get(ConfigDataKey.BOT_TOKEN), this); }
        catch (InterruptedException e) { throw new RuntimeException(e); }

        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle("✅ Proxy enabled!")
                        .setColor(Color.GREEN)
                        .build()
        );

        // Registering Chat Listener
        this.getProxy().getPluginManager().registerListener(this, new ServerChatHandler(this));
        discordBot.startChannelTopicUpdater();

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

    @NotNull
    public Config getConfig() {
        return config;
    }

    @NotNull
    public Bot getBot() {
        return discordBot;
    }

}
