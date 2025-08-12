package com.beanbeanjuice.simpleproxychat.commands;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.Map;

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

        HashMap<String, String> replacements = new HashMap<>(Map.of(
                "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString(),
                "message", CommonHelper.translateLegacyCodes(broadcastMessage)
        ));

        String broadcastString = config.get(ConfigKey.MINECRAFT_COMMAND_BROADCAST_MESSAGE).asString();
        broadcastString = CommonHelper.replaceKeys(broadcastString, replacements);

        for (Player player : plugin.getProxyServer().getAllPlayers())
            player.sendMessage(Helper.stringToComponent(broadcastString));
    }

    private void sendError(final CommandSource sender) {
        String errorString = config.get(ConfigKey.MINECRAFT_COMMAND_BROADCAST_USAGE).asString();
        errorString = CommonHelper.replaceKey(errorString, "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString());

        sender.sendMessage(Helper.stringToComponent(errorString));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_BROADCAST.getPermissionNode());
    }

}
