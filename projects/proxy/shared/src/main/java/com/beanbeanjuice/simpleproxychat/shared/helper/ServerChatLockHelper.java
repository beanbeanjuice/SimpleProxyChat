package com.beanbeanjuice.simpleproxychat.shared.helper;

import java.util.HashSet;

public class ServerChatLockHelper {

    private final HashSet<String> lockedServers;

    public ServerChatLockHelper() {
        lockedServers = new HashSet<>();
    }

    public void addServer(String server) {
        lockedServers.add(server);
    }

    public void removeServer(String server) {
        lockedServers.remove(server);
    }

    public boolean serverIsLocked(String server) {
        return lockedServers.contains(server);
    }

}
