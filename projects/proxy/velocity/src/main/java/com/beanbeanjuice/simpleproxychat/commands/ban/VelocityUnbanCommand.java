package com.beanbeanjuice.simpleproxychat.commands.ban;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VelocityUnbanCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityUnbanCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!config.get(ConfigKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM).asBoolean()) {
            invocation.source().sendMessage(Helper.stringToComponent("&cThe banning system is disabled..."));
            return;
        }

        if (invocation.arguments().length != 1) {
            String errorMessage = config.get(ConfigKey.MINECRAFT_COMMAND_PROXY_BAN_USAGE).asString();
            invocation.source().sendMessage(Helper.stringToComponent(errorMessage));
            return;
        }

        String playerName = invocation.arguments()[0];
        plugin.getBanHelper().removeBan(playerName);

        HashMap<String, String> replacements = new HashMap<>(Map.of(
                "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString(),
                "player", playerName
        ));

        String unbannedMessage = config.get(ConfigKey.MINECRAFT_COMMAND_PROXY_BAN_UNBANNED).asString();
        unbannedMessage = CommonHelper.replaceKeys(unbannedMessage, replacements);

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
