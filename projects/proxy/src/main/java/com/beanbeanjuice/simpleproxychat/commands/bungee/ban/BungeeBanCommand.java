package com.beanbeanjuice.simpleproxychat.commands.bungee.ban;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;

public class BungeeBanCommand extends Command implements TabExecutor {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeBanCommand(final SimpleProxyChatBungee plugin, final String... aliases) {
        super("Spc-ban", Permission.COMMAND_BAN.getPermissionNode(), aliases);
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!config.get(ConfigKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM).asBoolean()) {
            sender.sendMessage(Helper.convertToBungee("&cThe banning system is disabled..."));
            return;
        }

        if (args.length != 1) {
            String errorMessage = config.get(ConfigKey.MINECRAFT_COMMAND_PROXY_BAN_USAGE).asString();
            sender.sendMessage(Helper.convertToBungee(errorMessage));
            return;
        }

        String playerName = args[0];
        plugin.getBanHelper().addBan(playerName);
        plugin.getProxy().getPlayer(playerName).disconnect(Helper.convertToBungee("&cYou have been banned from the proxy."));

        String bannedMessage = config.get(ConfigKey.MINECRAFT_COMMAND_PROXY_BAN_BANNED).asString();
        bannedMessage = Helper.replaceKeys(
                bannedMessage,
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()),
                Tuple.of("player", playerName)
        );

        sender.sendMessage(Helper.convertToBungee(bannedMessage));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return plugin.getProxy().getPlayers()
                    .stream()
                    .map(CommandSender::getName)
                    .filter((bannedPlayer) -> bannedPlayer.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
