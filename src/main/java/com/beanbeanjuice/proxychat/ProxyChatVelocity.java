package com.beanbeanjuice.proxychat;

import com.google.inject.Inject;
import com.beanbeanjuice.proxychat.chat.ChatHandler;
import com.beanbeanjuice.proxychat.chat.VelocityServerListener;
import com.beanbeanjuice.proxychat.discord.Bot;
import com.beanbeanjuice.proxychat.utility.Helper;
import com.beanbeanjuice.proxychat.utility.config.Config;
import com.beanbeanjuice.proxychat.utility.config.ConfigDataEntry;
import com.beanbeanjuice.proxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "proxychat",
        name = "ProxyChat",
        version = "0.0.0",
        description = "A simple plugin to send chat messages between servers.",
        url = "https://www.plyblox.com",
        authors = {"beanbeanjuice"},
        dependencies = {
                @Dependency(id = "supervanish", optional = true),
                @Dependency(id = "premiumvanish", optional = true)
        }
)
public class ProxyChatVelocity {

    @Getter
    private final ProxyServer proxyServer;

    @Getter
    private final Logger logger;

    @Getter
    private Config config;

    @Getter
    private Bot discordBot;

    @Inject
    public ProxyChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;

        this.logger.info("The plugin is starting.");

        this.config = new Config(dataDirectory.toFile());
        this.config.initialize();

        PluginManager pm = this.proxyServer.getPluginManager();

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish").isPresent() || pm.getPlugin("SuperVanish").isPresent()) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.logger.info("Enabled PremiumVanish/SuperVanish Support");
        }

        this.logger.info("Initializing discord bot.");

        try { discordBot = new Bot((String) this.config.get(ConfigDataKey.BOT_TOKEN), this.config); }
        catch (Exception e) { logger.warn("There was an error starting the discord bot: " + e.getMessage()); }

        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle("✅ Proxy enabled!")
                        .setColor(Color.GREEN)
                        .build()
        );

        this.logger.info("Plugin has been initialized.");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Register Chat Listener
        ChatHandler chatHandler = new ChatHandler(config, discordBot, (message) -> {
            Component messageComponent = MiniMessage.miniMessage().deserialize(message);
            logger.info(Helper.stripColor(messageComponent));
            proxyServer.getAllPlayers().stream().forEach((player) -> player.sendMessage(messageComponent));
        });
        this.proxyServer.getEventManager().register(this, new VelocityServerListener(this, chatHandler));

        // Start Channel Topic Updater
        this.proxyServer.getScheduler()
                .buildTask(this, () -> {
                    discordBot.channelUpdaterFunction(proxyServer.getPlayerCount());
                })
                .delay(5, TimeUnit.MINUTES)
                .repeat(5, TimeUnit.MINUTES)
                .schedule();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        discordBot.sendMessageEmbed(
                new EmbedBuilder()
                        .setTitle("⛔ Proxy disabled.")
                        .setColor(Color.RED)
                        .build()
        );
    }

}
