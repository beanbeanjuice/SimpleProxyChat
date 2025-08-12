package com.beanbeanjuice.simpleproxychat.shared.helper;

import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.shared.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;

import java.util.UUID;

public class Helper {

    public static String convertAlias(Config config, String serverName) {
        String alias = config.get(ConfigKey.ALIASES).asStringMap().get(serverName);
        return (alias == null) ? serverName : alias;
    }

    public static String sanitize(String message) {
        return stripColor(MiniMessage.miniMessage().deserialize(message));
    }

    public static String stripColor(Component input) {
        return PlainTextComponentSerializer.plainText().serialize(input);
    }

    public static BaseComponent[] convertToBungee(String message) {
        message = CommonHelper.translateLegacyCodes(message);
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }

    public static Component stringToComponent(String string) {
        string = CommonHelper.translateLegacyCodes(string);
        return MiniMessage.miniMessage().deserialize(string);
    }

    public static boolean serverHasChatLocked(ISimpleProxyChat plugin, String serverName) {
        if (plugin.isNetworkManagerEnabled() && plugin.getNetworkManager().map(NetworkManagerPlugin.class::cast).map((nm) -> nm.getChatManager().isChatLocked(serverName) || nm.getChatManager().isChatLocked("all")).orElse(false))
            return true;

        if (plugin.getSPCConfig().getServerChatLockHelper().serverIsLocked(serverName)) return true;

        // TODO: Other methods of checking if chat is locked.
        return false;
    }

    public static boolean playerCanChat(ISimpleProxyChat plugin, UUID playerUUID, String playerName) {
        if (plugin.isLiteBansEnabled() && plugin.getLiteBansDatabase().map(Database.class::cast).map((db) -> db.isPlayerMuted(playerUUID, null)).orElse(false))
            return false;

        if (plugin.isAdvancedBanEnabled() && plugin.getAdvancedBanUUIDManager().map(UUIDManager.class::cast).map((uuidManager) -> {
            String uuid = uuidManager.getUUID(playerName);

            return plugin.getAdvancedBanPunishmentManager().map(PunishmentManager.class::cast).map((punishmentManager) -> punishmentManager.isMuted(uuid)).orElse(false);
        }).orElse(false))
            return false;

        // TODO: Other methods of checking if player can talk.
        return true;
    }

    public static String escapeString(String string) {
        return string.replaceAll("_", "\\_");
    }

}
