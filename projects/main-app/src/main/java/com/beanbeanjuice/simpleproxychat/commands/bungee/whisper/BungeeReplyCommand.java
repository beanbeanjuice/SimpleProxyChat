package com.beanbeanjuice.simpleproxychat.commands.bungee.whisper;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatBungee;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

public class BungeeReplyCommand extends Command {

    private final SimpleProxyChatBungee plugin;
    private final Config config;

    public BungeeReplyCommand(final SimpleProxyChatBungee plugin, String... aliases) {
        super("Spc-reply", Permission.COMMAND_WHISPER.getPermissionNode(), aliases);
        this.plugin = plugin;;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.getWhisperHandler().getLink(sender.getName()).map((playerName) -> plugin.getProxy().getPlayer(playerName)).ifPresentOrElse(
                (receiver) -> {
                    String message = Helper.translateLegacyCodes(String.join(" ", args));

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
                },
                () -> sender.sendMessage(Helper.convertToBungee(config.getAsString(ConfigDataKey.MINECRAFT_WHISPER_ERROR)))
        );

    }

}
