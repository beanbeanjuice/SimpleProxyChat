package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;

public class VelocityReplyCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityReplyCommand(SimpleProxyChatVelocity plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void execute(Invocation invocation) {
        plugin.getProxyServer().getPlayer(plugin.getWhisperHandler().getLink(((Player) invocation.source()).getUsername()).orElse("")).ifPresentOrElse(
                (receiver) -> {
                    String message = Helper.translateLegacyCodes(String.join(" ", invocation.arguments()));

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
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_WHISPER.getPermissionNode());
    }

}
