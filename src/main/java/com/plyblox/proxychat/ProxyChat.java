package com.plyblox.proxychat;

import com.plyblox.proxychat.chat.ServerChatHandler;
import com.plyblox.proxychat.discord.Bot;
import com.plyblox.proxychat.utility.config.Config;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class ProxyChat extends Plugin {

    private Config config;
    private Bot discordBot;

    @Override
    public void onEnable() {
        this.config = new Config(this);
        this.config.initialize();

        this.getLogger().log(Level.INFO, "The plugin is starting.");
        this.getLogger().log(Level.INFO, "Initializing discord bot.");

        try {
            discordBot = new Bot((String) config.get(ConfigDataKey.BOT_TOKEN), this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        discordBot.sendMessage("The proxy is now running!");

        // Registering Chat Listener
        this.getProxy().getPluginManager().registerListener(this, new ServerChatHandler(this));
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "The plugin is shutting down.");
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
