package com.beanbeanjuice.simpleproxychat.test.common;

import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommonHelperTest {

    @Test
    @DisplayName("Legacy Essentials Hex Codes Convert Properly")
    public void testTranslateLegacyEssentialsHexCodes() {
        Assertions.assertEquals("<#FFC0CB>Hello!", CommonHelper.translateLegacyCodes("&#FFC0CBHello!"));
    }

    @Test
    @DisplayName("Legacy Essentials Color Codes Convert Properly")
    public void testTranslateLegacyEssentialsColorCodes() {
        String originalText = "&6&lHello&b, &r&c&nworld!";

        String expectedText = String.format(
                "<%s><bold>Hello<%s>, <reset><%s><underlined>world!",
                NamedTextColor.GOLD.asHexString(),
                NamedTextColor.AQUA.asHexString(),
                NamedTextColor.RED.asHexString()
                );

        Assertions.assertEquals(expectedText, CommonHelper.translateLegacyCodes(originalText));
    }

    @Test
    @DisplayName("Legacy Essentials Color COdes Symbols Convert Properly")
    public void testTranslateLegacyEssentialsColorCodesSymbols() {
        String originalText = "§6§lHello§b, §r§c§nworld!";

        String expectedText = String.format(
                "<%s><bold>Hello<%s>, <reset><%s><underlined>world!",
                NamedTextColor.GOLD.asHexString(),
                NamedTextColor.AQUA.asHexString(),
                NamedTextColor.RED.asHexString()
        );

        Assertions.assertEquals(expectedText, CommonHelper.translateLegacyCodes(originalText));
    }

    @Test
    @DisplayName("Essentials Symbol Codes Are Converted Properly")
    public void testEssentialsSymbolCOdesAreConvertedProperly() {
        String originalText = "§x§f§b§6§3§f§5Hello!";
        String expectedText = "<#fb63f5>Hello!";

        Assertions.assertEquals(expectedText, CommonHelper.translateLegacyCodes(originalText));
    }

    @Test
    @DisplayName("Minimessage Strings Are Not Converted")
    public void testMinimessageStringsAreNotConverted() {
        String originalText = "<#FFC0CB><underlined>Hello, world!";

        Assertions.assertEquals(originalText, CommonHelper.translateLegacyCodes(originalText));
    }
}
