package com.beanbeanjuice.simpleproxychat.utility.config;

import lombok.Getter;

public enum Permission {

    READ_CHAT_MESSAGE("simpleproxychat.read.chat"),
    READ_JOIN_MESSAGE("simpleproxychat.read.join"),
    READ_LEAVE_MESSAGE("simpleproxychat.read.leave"),
    READ_FAKE_MESSAGE("simpleproxychat.read.fake"),
    READ_SWITCH_MESSAGE("simpleproxychat.read.switch"),
    READ_UPDATE_NOTIFICATION("simpleproxychat.read.update"),
    COMMAND_TOGGLE_CHAT("simpleproxychat.toggle.chat"),
    COMMAND_TOGGLE_CHAT_ALL("simpleproxychat.toggle.chat.all"),
    COMMAND_RELOAD("simpleproxychat.reload"),
    COMMAND_BAN("simpleproxychat.ban"),
    COMMAND_UNBAN("simpleproxychat.unban"),
    COMMAND_WHISPER("simpleproxychat.whisper"),
    COMMAND_BROADCAST("simpleproxychat.broadcast");

    @Getter private final String permissionNode;

    Permission(String permissionNode) {
        this.permissionNode = permissionNode;
    }

}
