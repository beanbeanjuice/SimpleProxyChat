package com.beanbeanjuice.simpleproxychat.utility.config;

import lombok.Getter;

public enum Permission {

    READ_JOIN_MESSAGE("simpleproxychat.read.join"),
    READ_LEAVE_MESSAGE("simpleproxychat.read.leave"),
    READ_FAKE_MESSAGE("simpleproxychat.read.fake"),
    READ_SWITCH_MESSAGE("simpleproxychat.read.switch"),
    READ_UPDATE_NOTIFICATION("simpleproxychat.read.update"),
    RELOAD("simpleproxychat.reload");

    @Getter private final String permissionNode;

    Permission(String permissionNode) {
        this.permissionNode = permissionNode;
    }

}
