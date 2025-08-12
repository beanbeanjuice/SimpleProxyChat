package com.beanbeanjuice.simpleproxychat.commands;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.HashMap;
import java.util.Map;

public class BungeeBroadcastCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeBroadcastCommand(final SimpleProxyChatBungee plugin, final String... aliases) {
        super("Spc-broadcast", Permission.COMMAND_BROADCAST.getPermissionNode(), aliases);

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

        HashMap<String, String> replacements = new HashMap<>(Map.of(
                "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString(),
                "message", CommonHelper.translateLegacyCodes(broadcastMessage)
        ));

        String broadcastString = config.get(ConfigKey.MINECRAFT_COMMAND_BROADCAST_MESSAGE).asString();

        broadcastString = CommonHelper.replaceKeys(broadcastString, replacements);

        for (ProxiedPlayer player : plugin.getProxy().getPlayers())
            player.sendMessage(Helper.convertToBungee(broadcastString));
    }

    private void sendError(final CommandSender sender) {
        String errorString = config.get(ConfigKey.MINECRAFT_COMMAND_BROADCAST_USAGE).asString();
        errorString = CommonHelper.replaceKey(errorString, "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString());

        sender.sendMessage(Helper.convertToBungee(errorString));
    }

}
