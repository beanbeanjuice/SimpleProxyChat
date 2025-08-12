package com.beanbeanjuice.simpleproxychat.shared.helper;

import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.utility.datastructures.BoundedArrayList;

public class LastMessagesHelper {

    private final Config config;
    private BoundedArrayList<String> boundedArrayList;

    public LastMessagesHelper(final Config config) {
        this.config = config;
        boundedArrayList = new BoundedArrayList<>(config.get(ConfigKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT).asInt());
        config.addReloadListener(this::reset);
    }

    private void reset() {
        BoundedArrayList<String> old = boundedArrayList;
        boundedArrayList = new BoundedArrayList<>(config.get(ConfigKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT).asInt());
        boundedArrayList.addAll(old);
    }

    public void addMessage(final String message) {
        boundedArrayList.add(message);
    }

    public BoundedArrayList<String> getBoundedArrayList() {
        if (!config.get(ConfigKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_ENABLED).asBoolean()) return new BoundedArrayList<>(0);

        return boundedArrayList;
    }

}
