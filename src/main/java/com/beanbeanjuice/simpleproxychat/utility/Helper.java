package com.beanbeanjuice.simpleproxychat.utility;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class Helper {

    /**
     * @see <a href="https://docs.advntr.dev/minimessage/format.html">Mini-Message Decorations</a>
     */
    public static String translateLegacyCodes(@NotNull String string) {
        return string
                .replaceAll("&0", convertToTag(NamedTextColor.BLACK.asHexString()))
                .replaceAll("&1", convertToTag(NamedTextColor.DARK_BLUE.asHexString()))
                .replaceAll("&2", convertToTag(NamedTextColor.DARK_GREEN.asHexString()))
                .replaceAll("&3", convertToTag(NamedTextColor.DARK_AQUA.asHexString()))
                .replaceAll("&4", convertToTag(NamedTextColor.DARK_RED.asHexString()))
                .replaceAll("&5", convertToTag(NamedTextColor.DARK_PURPLE.asHexString()))
                .replaceAll("&6", convertToTag(NamedTextColor.GOLD.asHexString()))
                .replaceAll("&7", convertToTag(NamedTextColor.GRAY.asHexString()))
                .replaceAll("&8", convertToTag(NamedTextColor.DARK_GRAY.asHexString()))
                .replaceAll("&9", convertToTag(NamedTextColor.BLUE.asHexString()))
                .replaceAll("&a", convertToTag(NamedTextColor.GREEN.asHexString()))
                .replaceAll("&b", convertToTag(NamedTextColor.AQUA.asHexString()))
                .replaceAll("&c", convertToTag(NamedTextColor.RED.asHexString()))
                .replaceAll("&d", convertToTag(NamedTextColor.LIGHT_PURPLE.asHexString()))
                .replaceAll("&e", convertToTag(NamedTextColor.YELLOW.asHexString()))
                .replaceAll("&f", convertToTag(NamedTextColor.WHITE.asHexString()))
                .replaceAll("&k", convertToTag("obfuscated"))
                .replaceAll("&l", convertToTag("bold"))
                .replaceAll("&m", convertToTag("strikethrough"))
                .replaceAll("&n", convertToTag("underlined"))
                .replaceAll("&o", convertToTag("italic"))
                .replaceAll("&r", convertToTag("reset"))

                .replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");  // "&#FFC0CBHello! -> <#FFC0CB>Hello!
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

}
