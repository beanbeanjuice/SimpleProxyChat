package com.beanbeanjuice.simpleproxychat.commands.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeReloadCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeReloadCommand(final SimpleProxyChatBungee plugin, final String... aliases) {
        super("Spc-reload", null, aliases);
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.COMMAND_RELOAD.getPermissionNode()) && sender instanceof ProxiedPlayer) {
            String message = config.get(ConfigKey.MINECRAFT_COMMAND_NO_PERMISSION).asString();
            sender.sendMessage(Helper.convertToBungee(message));
            return;
        }

        config.reload();

        String message = config.get(ConfigKey.MINECRAFT_COMMAND_RELOAD).asString();
        message = Helper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
        );
        sender.sendMessage(Helper.convertToBungee(message));
    }
}
