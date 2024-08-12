package com.beanbeanjuice.simpleproxychat.utility.helper;

import com.beanbeanjuice.simpleproxychat.utility.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import litebans.api.Database;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

    /**
     * @see <a href="https://docs.advntr.dev/minimessage/format.html">Mini-Message Decorations</a>
     */
    public static String translateLegacyCodes(String string) {
        string = replaceEssentialsColorCodes(string);
        return string
                .replace('§', '&')
                .replace("&0", convertToTag(NamedTextColor.BLACK.asHexString()))
                .replace("&1", convertToTag(NamedTextColor.DARK_BLUE.asHexString()))
                .replace("&2", convertToTag(NamedTextColor.DARK_GREEN.asHexString()))
                .replace("&3", convertToTag(NamedTextColor.DARK_AQUA.asHexString()))
                .replace("&4", convertToTag(NamedTextColor.DARK_RED.asHexString()))
                .replace("&5", convertToTag(NamedTextColor.DARK_PURPLE.asHexString()))
                .replace("&6", convertToTag(NamedTextColor.GOLD.asHexString()))
                .replace("&7", convertToTag(NamedTextColor.GRAY.asHexString()))
                .replace("&8", convertToTag(NamedTextColor.DARK_GRAY.asHexString()))
                .replace("&9", convertToTag(NamedTextColor.BLUE.asHexString()))
                .replace("&a", convertToTag(NamedTextColor.GREEN.asHexString()))
                .replace("&b", convertToTag(NamedTextColor.AQUA.asHexString()))
                .replace("&c", convertToTag(NamedTextColor.RED.asHexString()))
                .replace("&d", convertToTag(NamedTextColor.LIGHT_PURPLE.asHexString()))
                .replace("&e", convertToTag(NamedTextColor.YELLOW.asHexString()))
                .replace("&f", convertToTag(NamedTextColor.WHITE.asHexString()))
                .replace("&k", convertToTag("obfuscated"))
                .replace("&l", convertToTag("bold"))
                .replace("&m", convertToTag("strikethrough"))
                .replace("&n", convertToTag("underlined"))
                .replace("&o", convertToTag("italic"))
                .replace("&r", convertToTag("reset"))
                .replace("\\n", convertToTag("newline"))

                .replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");  // "&#FFC0CBHello! -> <#FFC0CB>Hello!
    }

    public static String replaceEssentialsColorCodes(String string) {
        Pattern pattern = Pattern.compile("§x(§[0-9a-fA-F]){6}");  // "§x§f§b§6§3§f§5Hello!" -> "&#fb63f5Hello!"
        Matcher matcher = pattern.matcher(string);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hexColor = matcher.group(0)
                    .replace("§x", "")
                    .replace("§", "");
            matcher.appendReplacement(result, "&#" + hexColor);
        }

        matcher.appendTail(result);

        return result.toString();
    }

    public static String convertAlias(Config config, String serverName) {
        String alias = config.getAsStringMap(ConfigDataKey.ALIASES).get(serverName);
        return (alias == null) ? serverName : alias;
    }

    private static String convertToTag(String string) {
        return "<" + string + ">";
    }

    public static String sanitize(String message) {
        return stripColor(MiniMessage.miniMessage().deserialize(message));
    }

    public static String stripColor(Component input) {
        return PlainTextComponentSerializer.plainText().serialize(input);
    }

    public static BaseComponent[] convertToBungee(String message) {
        message = translateLegacyCodes(message);
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }

    public static Component stringToComponent(String string) {
        string = translateLegacyCodes(string);
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

    public static String replaceKeys(String string, List<Tuple<String, String>> entries) {
        for (Tuple<String, String> entry : entries)
            string = string.replace(String.format("%%%s%%", entry.getKey()), entry.getValue());

        return string;
    }

    @SafeVarargs
    public static String replaceKeys(String string, Tuple<String, String>... entries) {
        for (Tuple<String, String> entry : entries)
            string = string.replace(String.format("%%%s%%", entry.getKey()), entry.getValue());

        return string;
    }

}
