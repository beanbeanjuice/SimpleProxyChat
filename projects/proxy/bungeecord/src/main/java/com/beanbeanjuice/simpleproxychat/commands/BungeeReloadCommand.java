package com.beanbeanjuice.simpleproxychat.commands;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
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
        message = CommonHelper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
        );
        sender.sendMessage(Helper.convertToBungee(message));
    }
}
