package com.beanbeanjuice.simpleproxychat.commands.whisper;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.Permission;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.ArrayList;
import java.util.List;

public class VelocityReplyCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityReplyCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        plugin.getProxyServer().getPlayer(plugin.getWhisperHandler().getLink(((Player) invocation.source()).getUsername()).orElse("")).ifPresentOrElse(
                (receiver) -> {
                    String message = CommonHelper.translateLegacyCodes(String.join(" ", invocation.arguments()));

                    String senderString = config.get(ConfigKey.MINECRAFT_WHISPER_SEND).asString();
                    String receiverString = config.get(ConfigKey.MINECRAFT_WHISPER_RECEIVE).asString();

                    List<Tuple<String, String>> replacements = new ArrayList<>();
                    replacements.add(Tuple.of("sender", ((Player) invocation.source()).getUsername()));
                    replacements.add(Tuple.of("receiver", receiver.getUsername()));
                    replacements.add(Tuple.of("message", message));
                    replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

                    senderString = CommonHelper.replaceKeys(senderString, replacements);
                    receiverString = CommonHelper.replaceKeys(receiverString, replacements);

                    invocation.source().sendMessage(Helper.stringToComponent(senderString));
                    receiver.sendMessage(Helper.stringToComponent(receiverString));

                    plugin.getWhisperHandler().set(((Player) invocation.source()).getUsername(), receiver.getUsername());
                },
                () -> invocation.source().sendMessage(Helper.stringToComponent(config.get(ConfigKey.MINECRAFT_WHISPER_ERROR).asString()))
        );
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permission.COMMAND_WHISPER.getPermissionNode());
    }

}
