package com.plyblox.proxychat.utility.config;

import org.jetbrains.annotations.NotNull;

public class ConfigDataEntry {

    private final Object data;

    public ConfigDataEntry(@NotNull Object data) {
        this.data = data;
    }

    @NotNull
    public Object getData() {
        return data;
    }
}
