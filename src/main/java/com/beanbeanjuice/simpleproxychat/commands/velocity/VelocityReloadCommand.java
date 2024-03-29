package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;

public class VelocityReloadCommand implements SimpleCommand {

    private final Config config;

    public VelocityReloadCommand(Config config) {
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        config.reload();
        Component component = Helper.stringToComponent(config.getAsString(ConfigDataKey.MINECRAFT_SUCCESSFUL_RELOAD));
        source.sendMessage(component);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.RELOAD.getPermissionNode());
    }

}
