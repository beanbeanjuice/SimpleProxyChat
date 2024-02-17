package com.plyblox.proxychat.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;

public class Bot {

    JDA bot;

    public Bot(@NotNull String token) throws InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.watching("Proxy"));
        bot = builder.build().awaitReady();
    }

    public void sendMessage(@NotNull String channelID, @NotNull String message) {
        bot.getTextChannelById(channelID).sendMessage(message).queue();
    }

}
