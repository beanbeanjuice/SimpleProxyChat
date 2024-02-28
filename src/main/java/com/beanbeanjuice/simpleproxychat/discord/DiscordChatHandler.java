package com.beanbeanjuice.simpleproxychat.discord;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DiscordChatHandler extends ListenerAdapter {

    private final Config config;
    private final Consumer<MessageReceivedEvent> sendFromDiscord;

    public DiscordChatHandler(@NotNull Config config,
                              Consumer<MessageReceivedEvent> sendFromDiscord) {
        this.config = config;
        this.sendFromDiscord = sendFromDiscord;
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getChannel().getId().equalsIgnoreCase((String) config.get(ConfigDataKey.CHANNEL_ID))) return;
        if (event.getAuthor().isBot()) return;

        sendFromDiscord.accept(event);
    }

}
