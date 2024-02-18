package com.plyblox.proxychat.discord;

import com.plyblox.proxychat.ProxyChat;
import com.plyblox.proxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class Bot {

    private ProxyChat plugin;
    private JDA bot;

    public Bot(@NotNull String token, @NotNull ProxyChat plugin) throws InterruptedException {
        this.plugin = plugin;

        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.watching("Proxy"));
        bot = builder.build().awaitReady();
    }

    public void sendMessage(@NotNull String message) {
        bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).sendMessage(message).queue();
    }

    public void sendMessageEmbed(@NotNull MessageEmbed embed) {
        bot.getTextChannelById((String) plugin.getConfig().get(ConfigDataKey.CHANNEL_ID)).sendMessageEmbeds(embed).queue();
    }

}
