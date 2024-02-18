package com.plyblox.proxychat.discord;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.Helper;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class DiscordChatHandler extends ListenerAdapter {

    private final ProxyChat plugin;

    public DiscordChatHandler(@NotNull ProxyChat plugin) {
        this.plugin = plugin;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().getId().equalsIgnoreCase((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID))) return;
        if (event.getAuthor().isBot()) return;

        Member author = event.getMember();
        Role topRole = author.getRoles().get(0);
        String message = event.getMessage().getContentStripped();

        String[] minecraftMessageConfig = ((String) plugin.getConfig().get(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE)).split(" ");

        Stream<ComponentBuilder> components = Arrays.stream(minecraftMessageConfig)
                .map((string) -> {
                    String text = string
                            .replace("%role%", topRole.getName())
                            .replace("%user%", author.getEffectiveName())
                            .replace("%message%", message);

                    ComponentBuilder builder = new ComponentBuilder(Helper.translateColors(text));

                    if (string.equalsIgnoreCase("%role%")) builder.color(ChatColor.of(topRole.getColor())).bold(true);

                    return builder;
                });

        ComponentBuilder chatComponentBuilder = new ComponentBuilder();
        components.forEach((component) -> chatComponentBuilder.append(component.create()).append(" "));

        plugin.getProxy().broadcast(chatComponentBuilder.create());
    }

}
