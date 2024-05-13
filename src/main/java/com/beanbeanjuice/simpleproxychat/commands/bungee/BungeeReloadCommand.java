package com.beanbeanjuice.simpleproxychat.commands.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeReloadCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeReloadCommand(SimpleProxyChatBungee plugin, Config config) {
        super("Spc-reload");
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.COMMAND_RELOAD.getPermissionNode()) && sender instanceof ProxiedPlayer) {
            String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_NO_PERMISSION);
            sender.sendMessage(Helper.convertToBungee(message));
            return;
        }

        config.reload();
        plugin.getDiscordBot().updateActivity();

        String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_RELOAD);
        message = Helper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
        );
        sender.sendMessage(Helper.convertToBungee(message));
    }
}
