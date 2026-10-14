package com.beanbeanjuice.simpleproxychathelper.listeners;

import com.beanbeanjuice.simpleproxychathelper.HelperTcpClient;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpigotChatListener implements Listener {

    private final HelperTcpClient client;

    public SpigotChatListener(HelperTcpClient client) {
        this.client = client;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        client.sendChat(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                event.getMessage()
        );
    }

}
