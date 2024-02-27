package com.plyblox.proxychat;

import com.plyblox.proxychat.chat.BungeeServerListener;
import com.plyblox.proxychat.chat.ChatHandler;
import com.plyblox.proxychat.discord.Bot;
import com.plyblox.proxychat.utility.config.Config;
import com.plyblox.proxychat.utility.config.ConfigDataEntry;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class ProxyChatBungee extends Plugin {

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

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish") != null || pm.getPlugin("SuperVanish") != null) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.getLogger().log(Level.INFO, "Enabled PremiumVanish/SuperVanish Support");
        }

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
            this.getProxy().broadcast(new ComponentBuilder(message).create());
        });
        this.getProxy().getPluginManager().registerListener(this, new BungeeServerListener(this, chatHandler));

        // Discord Topic Updater
        this.getProxy().getScheduler().schedule(this, () -> {
            discordBot.channelUpdaterFunction(this.getProxy().getPlayers().size());
        }, 5, 5, TimeUnit.MINUTES);

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
