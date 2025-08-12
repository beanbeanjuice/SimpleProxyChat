package com.beanbeanjuice.simpleproxychat.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.format.NamedTextColor;

public final class CommonHelper {

    private CommonHelper() { }

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

    private static String replaceEssentialsColorCodes(String string) {
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

    private static String convertToTag(String string) {
        return "<" + string + ">";
    }

    /**
     * Replaces keys with the entries. Essentially a glorified String.format()
     * @param string The {@link String} message you want to have replacements done on.
     * @param entries The key-value pair of {@link String} to do the replacements for.
     * @return The new {@link String} with replaced values.
     */
    public static String replaceKeys(String string, HashMap<String, String> entries) {
        if (string == null || entries == null) return string;

        Pattern pattern = Pattern.compile("%(.*?)%");
        Matcher matcher = pattern.matcher(string);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = entries.getOrDefault(key, matcher.group(0)); // keep raw if not found
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Replaces a single key with the value.
     * @param string The {@link String string} you want to parse.
     * @param key The {@link String key} that you want to replace.
     * @param value The {@link String value} you want to replace it with.
     * @return The parsed {@link String}.
     */
    public static String replaceKey(String string, String key, String value) {
        return replaceKeys(string, new HashMap<>(Map.of(key, value)));
    }

}
