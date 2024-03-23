package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import de.myzelyam.api.vanish.BungeePlayerHideEvent;
import de.myzelyam.api.vanish.BungeePlayerShowEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeVanishListener implements Listener {

    private final BungeeServerListener listener;
    private final Config config;

    public BungeeVanishListener(BungeeServerListener listener, Config config) {
        this.listener = listener;
        this.config = config;
    }

    @EventHandler
    public void onVanish(BungeePlayerHideEvent event) {
        if (!config.getAsBoolean(ConfigDataKey.USE_FAKE_MESSAGES)) return;

        listener.leave(event.getPlayer(), true);
    }

    @EventHandler
    public void onAppear(BungeePlayerShowEvent event) {
        if (!config.getAsBoolean(ConfigDataKey.USE_FAKE_MESSAGES)) return;

        listener.join(event.getPlayer(), event.getPlayer().getServer(), true);
    }

}
