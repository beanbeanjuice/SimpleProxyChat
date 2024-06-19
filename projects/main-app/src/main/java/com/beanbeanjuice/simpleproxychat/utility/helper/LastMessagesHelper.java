package com.beanbeanjuice.simpleproxychat.utility.helper;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.datastructures.BoundedArrayList;

public class LastMessagesHelper {

    private final Config config;
    private BoundedArrayList<String> boundedArrayList;

    public LastMessagesHelper(final Config config) {
        this.config = config;
        boundedArrayList = new BoundedArrayList<>(config.getAsInteger(ConfigDataKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT));
        config.addReloadListener(this::reset);
    }

    private void reset() {
        BoundedArrayList<String> old = boundedArrayList;
        boundedArrayList = new BoundedArrayList<>(config.getAsInteger(ConfigDataKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT));
        boundedArrayList.addAll(old);
    }

    public void addMessage(final String message) {
        boundedArrayList.add(message);
    }

    public BoundedArrayList<String> getBoundedArrayList() {
        if (!config.getAsBoolean(ConfigDataKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_ENABLED)) return new BoundedArrayList<>(0);

        return boundedArrayList;
    }

}
