package com.beanbeanjuice.simpleproxychat.commands.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeBroadcastCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeBroadcastCommand(final SimpleProxyChatBungee plugin) {
        super("Spc-broadcast", Permission.COMMAND_BROADCAST.getPermissionNode(), "Spc-bc");

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length == 0) {
            sendError(commandSender);
            return;
        }

        String broadcastMessage = String.join(" ", strings);
        String broadcastString = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_BROADCAST_MESSAGE);

        broadcastString = Helper.replaceKeys(
                broadcastString,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                Tuple.of("message", Helper.translateLegacyCodes(broadcastMessage))
        );

        for (ProxiedPlayer player : plugin.getProxy().getPlayers())
            player.sendMessage(Helper.convertToBungee(broadcastString));
    }

    private void sendError(final CommandSender sender) {
        String errorString = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_BROADCAST_USAGE);
        errorString = Helper.replaceKeys(errorString, Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        sender.sendMessage(Helper.convertToBungee(errorString));
    }

}
