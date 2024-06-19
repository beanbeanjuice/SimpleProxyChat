package com.beanbeanjuice.simpleproxychathelper.config;

import java.util.HashMap;

public class Config {

    private final HashMap<ConfigKey, Boolean> options;

    public Config() {
        this.options = new HashMap<>();
        setup();
    }

    private void setup() {
        this.options.put(ConfigKey.PLACEHOLDER_API_SUPPORT, false);
    }

    public boolean getOption(ConfigKey key) {
        return this.options.get(key);
    }

    public void setOption(ConfigKey key, boolean value) {
        this.options.put(key, value);
    }

}
