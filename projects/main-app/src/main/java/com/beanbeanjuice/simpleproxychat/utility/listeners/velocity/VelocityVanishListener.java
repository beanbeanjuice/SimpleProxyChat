package com.beanbeanjuice.simpleproxychat.utility.listeners.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VelocityVanishListener {

    private final SimpleProxyChatVelocity plugin;
    private final VelocityServerListener listener;
    private final HashMap<Player, Boolean> vanishedPlayers = new HashMap<>();

    public VelocityVanishListener(SimpleProxyChatVelocity plugin, VelocityServerListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    // TODO: There will be a better way to do this once the API is updated.
    private boolean hasStateChange(Player player) {
        boolean currentState = VelocityVanishAPI.isInvisible(player);

        Boolean oldState = vanishedPlayers.put(player, currentState);
        return oldState != null && oldState != currentState;
    }

    private void checkStateChange() {
        if (!plugin.getConfig().getAsBoolean(ConfigDataKey.VANISH_ENABLED)) return;

        plugin.getProxyServer().getAllPlayers().forEach(player -> {
            if (hasStateChange(player)) {
                if (vanishedPlayers.get(player)) listener.leave(player);
                else player.getCurrentServer().ifPresent(server -> this.listener.join(player, server.getServerInfo().getName()));
            }
        });
    }

    public void startVanishListener() {
        this.plugin.getProxyServer().getScheduler()
                .buildTask(plugin, this::checkStateChange)
                .repeat(500, TimeUnit.MILLISECONDS)
                .schedule();
    }

}
