package com.beanbeanjuice.simpleproxychat.shared.socket;

import com.beanbeanjuice.simpleproxychat.shared.utility.listeners.MessageType;
import lombok.Getter;

import java.util.UUID;

public abstract class ChatMessageData extends MessageData {

    @Getter private final String servername;
    @Getter private final String playerName;
    @Getter private final UUID playerUUID;
    @Getter private final String message;

    // Chat
    public ChatMessageData(MessageType type, String servername, String playerName,
                           UUID playerUUID, String message) {
        super(type);
        this.servername = servername;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.message = message;
    }

    public abstract void chatSendToAllOtherPlayers(String parsedMessage);

}
