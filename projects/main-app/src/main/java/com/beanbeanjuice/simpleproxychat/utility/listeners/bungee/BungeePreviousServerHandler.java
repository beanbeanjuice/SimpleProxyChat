package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.Optional;

public class BungeePreviousServerHandler {

    // TODO: Use UUID, String is insecure.
    private final HashMap<String, ServerInfo> previousServers = new HashMap<>();

    public void put(final String playerName, final ServerInfo serverInfo) {
        previousServers.put(playerName, serverInfo);
    }

    public Optional<ServerInfo> get(final String playerName) {
        return Optional.ofNullable(previousServers.get(playerName));
    }

}
