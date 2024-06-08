package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VelocityWhisperCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityWhisperCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 2) {
            invocation.source().sendMessage(Helper.stringToComponent(config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_ERROR)));
            return;
        }

        plugin.getProxyServer().getPlayer(invocation.arguments()[0]).ifPresentOrElse(
                (receiver) -> {
                    String message = Helper.translateLegacyCodes(Arrays.stream(invocation.arguments()).skip(1).collect(Collectors.joining(" ")));

                    String senderString = config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_SEND);
                    String receiverString = config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_RECEIVE);

                    List<Tuple<String, String>> replacements = new ArrayList<>();
                    replacements.add(Tuple.of("sender", ((Player) invocation.source()).getUsername()));
                    replacements.add(Tuple.of("receiver", receiver.getUsername()));
                    replacements.add(Tuple.of("message", message));
                    replacements.add(Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)));

                    senderString = Helper.replaceKeys(senderString, replacements);
                    receiverString = Helper.replaceKeys(receiverString, replacements);

                    invocation.source().sendMessage(Helper.stringToComponent(senderString));
                    receiver.sendMessage(Helper.stringToComponent(receiverString));

                    plugin.getWhisperHandler().set(((Player) invocation.source()).getUsername(), receiver.getUsername());
                },
                () -> invocation.source().sendMessage(Helper.stringToComponent(config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_ERROR)))
        );
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length <= 1) return plugin.getProxyServer().getAllPlayers().stream().map((Player::getUsername)).filter((username) -> {
            if (invocation.arguments().length == 1) {
                return username.toLowerCase().startsWith(invocation.arguments()[0].toLowerCase());
            }
            return true;
        }).toList();
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_WHISPER.getPermissionNode());
    }
}
