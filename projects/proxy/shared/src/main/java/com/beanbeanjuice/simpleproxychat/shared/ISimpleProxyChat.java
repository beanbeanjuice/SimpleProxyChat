package com.beanbeanjuice.simpleproxychat.shared;

import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.discord.Bot;
import com.beanbeanjuice.simpleproxychat.shared.socket.HelperTcpServer;

import java.util.Optional;

public interface ISimpleProxyChat {

    boolean isPluginStarting();

    boolean isLuckPermsEnabled();
    Optional<?> getLuckPerms();

    boolean isVanishAPIEnabled();

    boolean isLiteBansEnabled();
    Optional<?> getLiteBansDatabase();

    boolean isAdvancedBanEnabled();
    Optional<?> getAdvancedBanUUIDManager();
    Optional<?> getAdvancedBanPunishmentManager();

    boolean isNetworkManagerEnabled();
    Optional<?> getNetworkManager();

    Optional<HelperTcpServer> getHelperTcpServer();

    Config getSPCConfig();
    Bot getDiscordBot();
    void sendAll(String message);
    void log(String message);

}
