package com.beanbeanjuice.simpleproxychat.utility.epoch;

import java.time.Instant;

public class EpochHelper {

    public static long getEpochMillisecond() {
        return System.currentTimeMillis();
    }

    public static Instant getEpochInstant() {
        return Instant.ofEpochMilli(getEpochMillisecond());
    }

    public static long getEpochSecond() {
        return getEpochMillisecond() / 1000;
    }

}
