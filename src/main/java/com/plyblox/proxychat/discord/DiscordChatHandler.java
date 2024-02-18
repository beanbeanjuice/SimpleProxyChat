package com.plyblox.proxychat.discord;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

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

        TextComponent roleText = new TextComponent(topRole.getName());
        roleText.setColor(ChatColor.of(topRole.getColor()));

        String minecraftMessageConfig = (String) plugin.getConfig().get(ConfigDataKey.DISCORD_TO_MINECRAFT_MESSAGE);
        String roleColorString = ChatColor.of(topRole.getColor()).toString();

        String minecraftMessage = minecraftMessageConfig
                .replace("%role%", roleColorString)
                .replace("%user%", author.getEffectiveName())
                .replace("%message%", message);

        plugin.getProxy().broadcast(new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', minecraftMessage)).create());
    }

}
