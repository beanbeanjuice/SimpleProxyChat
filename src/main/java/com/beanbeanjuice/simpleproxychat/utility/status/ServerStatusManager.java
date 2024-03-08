package com.beanbeanjuice.simpleproxychat.utility.status;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Hashtable;

public class ServerStatusManager {

    private final Hashtable<String, ServerStatus> servers;  // Hashtable for Thread Safety
    private final Config config;

    public ServerStatusManager(Config config) {
        servers = new Hashtable<>();
        this.config = config;
    }

    public ServerStatus getStatus(String serverName) {
        servers.putIfAbsent(serverName, new ServerStatus());
        return servers.get(serverName);
    }

    public MessageEmbed getStatusEmbed(String serverName, boolean status) {
        String statusMessageString = config.getAsString(ConfigDataKey.PROXY_STATUS_MESSAGE);
        String statusString = status ? config.getAsString(ConfigDataKey.PROXY_STATUS_ONLINE) : config.getAsString(ConfigDataKey.PROXY_STATUS_OFFLINE);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(config.getAsString(ConfigDataKey.PROXY_STATUS_TITLE));
        embedBuilder.addField(
                Helper.convertAlias(config, serverName),
                String.format("%s%s", statusMessageString, statusString),
                true);
        embedBuilder.setColor(status ? Color.GREEN : Color.RED);
        return embedBuilder.build();
    }

    public String getStatusString(String serverName, boolean status) {
        String statusString = status ? "online" : "offline";
        return String.format("%s is %s.", Helper.convertAlias(config, serverName), statusString);
    }
}
