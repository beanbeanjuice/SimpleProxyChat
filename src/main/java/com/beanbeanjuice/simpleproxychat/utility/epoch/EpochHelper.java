package com.beanbeanjuice.simpleproxychat.utility.epoch;

import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;

public class EpochHelper {

    private final Config config;
    private Long epochCache;
    private long timeAtLastRefresh;

    public EpochHelper(Config config) {
        this.config = config;
        epochCache = System.currentTimeMillis();
        timeAtLastRefresh = System.currentTimeMillis();
    }

    public long getEpochMillisecond() {
        if (!config.getAsBoolean(ConfigDataKey.TIMESTAMP_USE_API)) return System.currentTimeMillis();

        Optional<Long> optionalEpochLong = getEpochFromAPI();
        if (optionalEpochLong.isEmpty()) return epochCache;
        epochCache = optionalEpochLong.get();
        return epochCache;
    }

    public Instant getEpochInstant() {
        return Instant.ofEpochMilli(epochCache);
    }

    public long getEpochSecond() {
        return getEpochMillisecond() / 1000;
    }

    private Optional<Long> getEpochFromAPI() {
        if (System.currentTimeMillis() - timeAtLastRefresh < 15000) return Optional.of(epochCache);

        try (InputStream is = new URI("https://currentmillis.com/time/minutes-since-unix-epoch.php").toURL().openStream(); Scanner scann = new Scanner(is)) {
            if (scann.hasNext()) {
                timeAtLastRefresh = System.currentTimeMillis();
                return Optional.of(Long.parseLong(scann.next()) * 60000);
            }
        } catch (IOException | URISyntaxException ignored) { }
        return Optional.empty();
    }

}
