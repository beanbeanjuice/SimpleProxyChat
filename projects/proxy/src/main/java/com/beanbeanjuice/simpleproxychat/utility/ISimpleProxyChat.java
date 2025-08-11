package com.beanbeanjuice.simpleproxychat.utility;

import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;

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

    Config getSPCConfig();
    Bot getDiscordBot();
    void sendAll(String message);
    void log(String message);

}
