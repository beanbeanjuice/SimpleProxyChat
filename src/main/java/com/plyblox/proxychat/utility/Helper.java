package com.plyblox.proxychat.utility;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Helper {

    public static String translateColors(@NotNull String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
