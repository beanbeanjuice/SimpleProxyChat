package com.beanbeanjuice.simpleproxychat.utility;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;

public class ServerStatusManager {

    private final HashMap<String, Boolean> servers;
    private final Config config;

    public ServerStatusManager(Config config) {
        servers = new HashMap<>();
        this.config = config;
    }

    public Optional<Boolean> setStatus(String serverName, Boolean status) {
        return Optional.ofNullable(servers.put(serverName, status));
    }

    public MessageEmbed getStatusEmbed(String serverName, boolean status) {
        String statusMessageString = (String) config.get(ConfigDataKey.PROXY_STATUS_MESSAGE);
        String statusString = status ? (String) config.get(ConfigDataKey.PROXY_STATUS_ONLINE) : (String) config.get(ConfigDataKey.PROXY_STATUS_OFFLINE);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle((String) config.get(ConfigDataKey.PROXY_STATUS_TITLE));
        embedBuilder.addField(
                serverName.toUpperCase(),
                String.format("%s%s", statusMessageString, statusString),
                true);
        embedBuilder.setColor(status ? Color.GREEN : Color.RED);
        return embedBuilder.build();
    }

    public String getStatusString(String serverName, boolean status) {
        String statusString = status ? "online" : "offline";
        return String.format("%s is %s.", serverName.toUpperCase(), statusString);
    }
}
