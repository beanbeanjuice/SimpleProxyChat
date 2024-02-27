package com.beanbeanjuice.proxychat.chat;

import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeVanishListener implements Listener {

    private BungeeServerListener listener;

    public BungeeVanishListener(BungeeServerListener listener) {
        this.listener = listener;
    }

    @EventHandler
    public void onVanish(BungeePlayerHideEvent event) {
        listener.leave(event.getPlayer());
    }

    @EventHandler
    public void onAppear(BungeePlayerShowEvent event) {
        listener.join(event.getPlayer());
    }

}
