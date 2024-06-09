package com.beanbeanjuice.simpleproxychat.commands.bungee;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BungeeWhisperCommand extends Command implements TabExecutor {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeWhisperCommand(SimpleProxyChatBungee plugin, Config config, String... aliases) {
        super("Spc-whisper", Permission.COMMAND_WHISPER.getPermissionNode(), aliases);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Helper.convertToBungee(config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_ERROR)));
            return;
        }

        ProxiedPlayer receiver = plugin.getProxy().getPlayer(args[0]);
        if (receiver == null) {
            sender.sendMessage(Helper.convertToBungee(config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_ERROR)));
            return;
        }

        String message = Helper.translateLegacyCodes(Arrays.stream(args).skip(1).collect(Collectors.joining(" ")));

        String senderString = config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_SEND);
        String receiverString = config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_RECEIVE);

        List<Tuple<String, String>> replacements = new ArrayList<>();
        replacements.add(Tuple.of("sender", sender.getName()));
        replacements.add(Tuple.of("receiver", receiver.getName()));
        replacements.add(Tuple.of("message", message));
        replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

        senderString = Helper.replaceKeys(senderString, replacements);
        receiverString = Helper.replaceKeys(receiverString, replacements);

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
