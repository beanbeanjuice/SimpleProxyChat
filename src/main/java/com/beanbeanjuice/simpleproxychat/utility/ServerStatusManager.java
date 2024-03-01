package com.beanbeanjuice.simpleproxychat.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;

public class ServerStatusManager {

    private final HashMap<String, Boolean> servers;

    public ServerStatusManager() {
        servers = new HashMap<>();
    }

    public Optional<Boolean> setStatus(String serverName, Boolean status) {
        return Optional.ofNullable(servers.put(serverName, status));
    }

    public MessageEmbed getStatusEmbed(String serverName, boolean status) {
        String statusString = status ? "Online" : "Offline";

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Server Status");
        embedBuilder.addField(serverName.toUpperCase(), String.format("Status: %s", statusString), true);
        embedBuilder.setColor(status ? Color.GREEN : Color.RED);
        return embedBuilder.build();
    }

    public String getStatusString(String serverName, boolean status) {
        return String.format("%s is %s", serverName.toUpperCase(), status);
    }
}
