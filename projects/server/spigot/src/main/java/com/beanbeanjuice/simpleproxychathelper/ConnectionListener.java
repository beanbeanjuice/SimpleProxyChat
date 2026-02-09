package com.beanbeanjuice.simpleproxychathelper;

import com.beanbeanjuice.simpleproxychathelper.shared.config.ConfigKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final SimpleProxyChatHelper plugin;

    public ConnectionListener(SimpleProxyChatHelper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getOptions().getOption(ConfigKey.SILENT_JOIN)) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getOptions().getOption(ConfigKey.SILENT_QUIT)) {
            event.setQuitMessage(null);
        }
    }

}
