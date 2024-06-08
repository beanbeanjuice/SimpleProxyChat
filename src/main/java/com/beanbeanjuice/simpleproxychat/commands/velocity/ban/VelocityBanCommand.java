package com.beanbeanjuice.simpleproxychat.commands.velocity.ban;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.md_5.bungee.api.CommandSender;

import java.util.List;

public class VelocityBanCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityBanCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
            invocation.source().sendMessage(Helper.stringToComponent("&cThe banning system is disabled..."));
            return;
        }

        if (invocation.arguments().length != 1) {
            String errorMessage = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_PROXY_BAN_USAGE);
            invocation.source().sendMessage(Helper.stringToComponent(errorMessage));
            return;
        }

        String playerName = invocation.arguments()[0];
        plugin.getBanHelper().addBan(playerName);
        plugin.getProxyServer().getPlayer(playerName).ifPresent(player -> player.disconnect(Helper.stringToComponent("&cYou have been banned from the proxy.")));

        String bannedMessage = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_PROXY_BAN_BANNED);
        bannedMessage = Helper.replaceKeys(
                bannedMessage,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                Tuple.of("player", playerName)
        );

        invocation.source().sendMessage(Helper.stringToComponent(bannedMessage));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            return plugin.getProxyServer().getAllPlayers().stream().map(Player::getUsername).toList();
        }

        if (invocation.arguments().length == 1) {
            return plugin.getProxyServer().getAllPlayers()
                    .stream()
                    .map(Player::getUsername)
                    .filter((bannedPlayer) -> bannedPlayer.toLowerCase().startsWith(invocation.arguments()[0].toLowerCase()))
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_BAN.getPermissionNode());
    }

}
