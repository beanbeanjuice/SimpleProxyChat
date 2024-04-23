package com.beanbeanjuice.simpleproxychat.utility.listeners.bungee;

import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;
import java.util.UUID;

public class BungeePreviousServerHandler {

    private final HashMap<UUID, ServerInfo> previousServers = new HashMap<>();

    public void put(final UUID uuid, final ServerInfo serverInfo) {
        previousServers.put(uuid, serverInfo);
    }

    public ServerInfo get(final UUID uuid) {
        return previousServers.get(uuid);
    }

}
