package com.beanbeanjuice.proxychat.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class Helper {

//    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-ORX]");

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
                .replaceAll("&f", convertToTag(NamedTextColor.WHITE.asHexString()));
    }

    private static String convertToTag(String string) {
        return "<" + string + ">";
    }

    public static String stripColor(Component input) {
        return PlainTextComponentSerializer.plainText().serialize(input);
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

}
