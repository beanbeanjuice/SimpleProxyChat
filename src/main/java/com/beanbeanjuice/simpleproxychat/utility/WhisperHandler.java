package com.beanbeanjuice.simpleproxychat.utility;

import java.util.HashMap;
import java.util.Optional;

public class WhisperHandler {

    private final HashMap<String, String> lastMessagedPlayer;

    public WhisperHandler() {
        lastMessagedPlayer = new HashMap<>();
    }

    public void set(String player1, String player2) {
        lastMessagedPlayer.put(player1, player2);
        lastMessagedPlayer.put(player2, player1);
    }

    public Optional<String> getLink(String playerName) {
        return Optional.ofNullable(lastMessagedPlayer.get(playerName));
    }

}
