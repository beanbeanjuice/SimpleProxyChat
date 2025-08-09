package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

public class VelocityReloadCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityReloadCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        config.reload();
        plugin.getDiscordBot().updateActivity();

        String message = config.get(ConfigKey.MINECRAFT_COMMAND_RELOAD).asString();
        message = Helper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
        );
        source.sendMessage(Helper.stringToComponent(message));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_RELOAD.getPermissionNode());
    }

}
