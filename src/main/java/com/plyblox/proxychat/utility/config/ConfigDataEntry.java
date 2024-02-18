package com.plyblox.proxychat.utility.config;

import org.jetbrains.annotations.NotNull;

public record ConfigDataEntry(Object data) {

    public ConfigDataEntry(@NotNull Object data) {
        this.data = data;
    }

    @Override
    @NotNull
    public Object data() {
        return data;
    }

}
