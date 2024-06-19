package com.beanbeanjuice.simpleproxychat.commands.bungee.ban;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;

public class BungeeUnbanCommand extends Command implements TabExecutor {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeUnbanCommand(final SimpleProxyChatBungee plugin) {
        super("Spc-unban", Permission.COMMAND_UNBAN.getPermissionNode());
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
            sender.sendMessage(Helper.convertToBungee("&cThe banning system is disabled..."));
            return;
        }

        if (args.length != 1) {
            String errorMessage = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_PROXY_BAN_USAGE);
            sender.sendMessage(Helper.convertToBungee(errorMessage));
            return;
        }

        String playerName = args[0];
        plugin.getBanHelper().removeBan(playerName);

        String unbannedMessage = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_PROXY_BAN_UNBANNED);
        unbannedMessage = Helper.replaceKeys(
                unbannedMessage,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                Tuple.of("player", playerName)
        );

        sender.sendMessage(Helper.convertToBungee(unbannedMessage));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getBanHelper().getBannedPlayers()
                    .stream()
                    .filter((bannedPlayer) -> bannedPlayer.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
