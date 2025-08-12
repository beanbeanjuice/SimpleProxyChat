package com.beanbeanjuice.simpleproxychat.commands.whisper;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class BungeeWhisperCommand extends Command implements TabExecutor {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeWhisperCommand(final SimpleProxyChatBungee plugin, final String... aliases) {
        super("Spc-whisper", Permission.COMMAND_WHISPER.getPermissionNode(), aliases);
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Helper.convertToBungee(config.get(ConfigKey.MINECRAFT_WHISPER_ERROR).asString()));
            return;
        }

        ProxiedPlayer receiver = plugin.getProxy().getPlayer(args[0]);
        if (receiver == null) {
            sender.sendMessage(Helper.convertToBungee(config.get(ConfigKey.MINECRAFT_WHISPER_ERROR).asString()));
            return;
        }

        String message = CommonHelper.translateLegacyCodes(Arrays.stream(args).skip(1).collect(Collectors.joining(" ")));

        String senderString = config.get(ConfigKey.MINECRAFT_WHISPER_SEND).asString();
        String receiverString = config.get(ConfigKey.MINECRAFT_WHISPER_RECEIVE).asString();

        HashMap<String, String> replacements = new HashMap<>(Map.of(
                "sender", sender.getName(),
                "receiver", receiver.getName(),
                "message", message,
                "plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()
        ));

        senderString = CommonHelper.replaceKeys(senderString, replacements);
        receiverString = CommonHelper.replaceKeys(receiverString, replacements);

        sender.sendMessage(Helper.convertToBungee(senderString));
        receiver.sendMessage(Helper.convertToBungee(receiverString));

        plugin.getWhisperHandler().set(sender.getName(), receiver.getName());
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1)
            return plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).filter((username) -> {
                if (args.length == 1) {
                    return username.toLowerCase().startsWith(args[0].toLowerCase());
                }
                return true;
            }).toList();

        return List.of();
    }
}
