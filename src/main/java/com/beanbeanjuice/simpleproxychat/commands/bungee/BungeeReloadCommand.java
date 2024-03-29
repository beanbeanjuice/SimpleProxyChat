package com.beanbeanjuice.simpleproxychat.commands.bungee;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeReloadCommand extends Command {

    private final Config config;

    public BungeeReloadCommand(Config config) {
        super("Spc-reload");
        this.config = config;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.COMMAND_RELOAD.getPermissionNode()) && sender instanceof ProxiedPlayer) {
            String message = config.getAsString(ConfigDataKey.MINECRAFT_NO_PERMISSION);
            sender.sendMessage(convertToBungee(message));
            return;
        }

        config.reload();
        String message = config.getAsString(ConfigDataKey.MINECRAFT_SUCCESSFUL_RELOAD);
        sender.sendMessage(convertToBungee(message));
    }

    private BaseComponent[] convertToBungee(String message) {
        Component minimessage = MiniMessage.miniMessage().deserialize(message);
        return BungeeComponentSerializer.get().serialize(minimessage);
    }
}
