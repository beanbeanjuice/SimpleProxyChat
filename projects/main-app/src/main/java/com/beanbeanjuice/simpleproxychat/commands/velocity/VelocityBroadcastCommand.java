package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class VelocityBroadcastCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityBroadcastCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            sendError(invocation.source());
            return;
        }

        String broadcastMessage = String.join(" ", invocation.arguments());
        String broadcastString = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_BROADCAST_MESSAGE);

        broadcastString = Helper.replaceKeys(
                broadcastString,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                Tuple.of("message", Helper.translateLegacyCodes(broadcastMessage))
        );

        for (Player player : plugin.getProxyServer().getAllPlayers())
            player.sendMessage(Helper.stringToComponent(broadcastString));
    }

    private void sendError(final CommandSource sender) {
        String errorString = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_BROADCAST_USAGE);
        errorString = Helper.replaceKeys(errorString, Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        sender.sendMessage(Helper.stringToComponent(errorString));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_BROADCAST.getPermissionNode());
    }

}
