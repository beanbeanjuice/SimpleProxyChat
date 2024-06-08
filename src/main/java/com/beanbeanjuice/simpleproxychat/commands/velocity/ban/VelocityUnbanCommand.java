package com.beanbeanjuice.simpleproxychat.commands.velocity.ban;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;

public class VelocityUnbanCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityUnbanCommand(final SimpleProxyChatVelocity plugin) {
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
        plugin.getBanHelper().removeBan(playerName);

        String unbannedMessage = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_PROXY_BAN_UNBANNED);
        unbannedMessage = Helper.replaceKeys(
                unbannedMessage,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                Tuple.of("player", playerName)
        );

        invocation.source().sendMessage(Helper.stringToComponent(unbannedMessage));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            return plugin.getBanHelper().getBannedPlayers();
        }

        if (invocation.arguments().length == 1) {
            return plugin.getBanHelper().getBannedPlayers()
                    .stream()
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
