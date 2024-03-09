package com.beanbeanjuice.simpleproxychat.utility.config;

import lombok.Getter;

public enum Permission {

    READ_JOIN_MESSAGE("simpleproxychat.read.join"),
    READ_LEAVE_MESSAGE("simpleproxychat.read.leave"),
    READ_SWITCH_MESSAGE("simpleproxychat.read.switch");

    @Getter private final String permissionNode;

    Permission(String permissionNode) {
        this.permissionNode = permissionNode;
    }

}
