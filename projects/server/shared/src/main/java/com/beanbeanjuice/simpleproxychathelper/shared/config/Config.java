package com.beanbeanjuice.simpleproxychathelper.shared.config;

import java.util.HashMap;

public class Config {

    private final HashMap<ConfigKey, Boolean> options;

    public Config() {
        this.options = new HashMap<>();
        setup();
    }

    private void setup() {
        this.options.put(ConfigKey.PLACEHOLDER_API_SUPPORT, false);
        this.options.put(ConfigKey.SILENT_JOIN, true);
        this.options.put(ConfigKey.SILENT_QUIT, true);
    }

    public boolean getOption(ConfigKey key) {
        return this.options.get(key);
    }

    public void setOption(ConfigKey key, boolean value) {
        this.options.put(key, value);
    }

}
