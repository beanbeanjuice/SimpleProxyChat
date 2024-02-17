package com.plyblox.proxychat;

import com.plyblox.proxychat.discord.Bot;
import com.plyblox.proxychat.utility.config.Config;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
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

        HashMap<String, List<String>> servers = (HashMap<String, List<String>>) config.get(ConfigDataKey.SERVERS);
        try {
            discordBot = new Bot((String) config.get(ConfigDataKey.BOT_TOKEN));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        discordBot.sendMessage(servers.get("factions").get(0), "Server is starting...");
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "The plugin is shutting down.");
    }

}
