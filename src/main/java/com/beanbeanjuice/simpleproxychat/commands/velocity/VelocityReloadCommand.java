package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.plugin.Plugin;
import net.kyori.adventure.text.Component;

public class VelocityReloadCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityReloadCommand(SimpleProxyChatVelocity plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        config.reload();
        plugin.getDiscordBot().updateActivity();

        Component component = Helper.stringToComponent(config.getAsString(ConfigDataKey.MINECRAFT_SUCCESSFUL_RELOAD));
        source.sendMessage(component);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_RELOAD.getPermissionNode());
    }

}
