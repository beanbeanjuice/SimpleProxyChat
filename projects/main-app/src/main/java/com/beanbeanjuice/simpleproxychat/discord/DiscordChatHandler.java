package com.beanbeanjuice.simpleproxychat.discord;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.Consumer;

public class DiscordChatHandler extends ListenerAdapter {

    private final Config config;
    private final Consumer<MessageReceivedEvent> sendFromDiscord;

    public DiscordChatHandler(Config config,
                              Consumer<MessageReceivedEvent> sendFromDiscord) {
        this.config = config;
        this.sendFromDiscord = sendFromDiscord;
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equalsIgnoreCase(config.get(ConfigKey.CHANNEL_ID).asString())) return;
        if (event.getAuthor().isBot()) return;
        if (!config.get(ConfigKey.DISCORD_CHAT_ENABLED).asBoolean()) return;

        sendFromDiscord.accept(event);
    }

}
