package com.beanbeanjuice.simpleproxychat.socket;

import com.beanbeanjuice.simpleproxychat.utility.listeners.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

public abstract class MessageData {

    @Getter private final MessageType type;

    @Setter private String minecraftMessage;
    @Setter private String discordMessage;
    @Setter private String discordEmbedTitle;
    @Setter private String discordEmbedMessage;

    public MessageData(MessageType type) {
        this.type = type;
    }

    public Optional<String> getMinecraftMessage() {
        return Optional.ofNullable(this.minecraftMessage);
    }

    public Optional<String> getDiscordMessage() {
        return Optional.ofNullable(this.discordMessage);
    }

    public Optional<String> getDiscordEmbedTitle() {
        return Optional.ofNullable(this.discordEmbedTitle);
    }

    public Optional<String> getDiscordEmbedMessage() {
        return Optional.ofNullable(this.discordEmbedMessage);
    }

    public abstract void startPluginMessage();
    public abstract byte[] getAsBytes();

}
