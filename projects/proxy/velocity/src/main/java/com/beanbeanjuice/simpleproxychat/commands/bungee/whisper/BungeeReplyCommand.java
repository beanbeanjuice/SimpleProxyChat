package com.beanbeanjuice.simpleproxychat.commands.bungee.whisper;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

public class BungeeReplyCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeReplyCommand(final SimpleProxyChatBungee plugin, final String... aliases) {
        super("Spc-reply", Permission.COMMAND_WHISPER.getPermissionNode(), aliases);
        this.plugin = plugin;;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getWhisperHandler().getLink(sender.getName()).map((playerName) -> plugin.getProxy().getPlayer(playerName)).ifPresentOrElse(
                (receiver) -> {
                    String message = CommonHelper.translateLegacyCodes(String.join(" ", args));

                    String senderString = config.get(ConfigKey.MINECRAFT_WHISPER_SEND).asString();
                    String receiverString = config.get(ConfigKey.MINECRAFT_WHISPER_RECEIVE).asString();

                    List<Tuple<String, String>> replacements = new ArrayList<>();
                    replacements.add(Tuple.of("sender", sender.getName()));
                    replacements.add(Tuple.of("receiver", receiver.getName()));
                    replacements.add(Tuple.of("message", message));
                    replacements.add(Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()));

                    senderString = CommonHelper.replaceKeys(senderString, replacements);
                    receiverString = CommonHelper.replaceKeys(receiverString, replacements);

                    sender.sendMessage(Helper.convertToBungee(senderString));
                    receiver.sendMessage(Helper.convertToBungee(receiverString));

                    plugin.getWhisperHandler().set(sender.getName(), receiver.getName());
                },
                () -> sender.sendMessage(Helper.convertToBungee(config.get(ConfigKey.MINECRAFT_WHISPER_ERROR).asString()))
        );

    }

}
