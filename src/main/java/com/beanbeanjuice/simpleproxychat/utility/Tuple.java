package com.beanbeanjuice.simpleproxychat.utility;

import lombok.Getter;

@Getter
public class Tuple<KeyType, ValueType> {

    private final KeyType key;
    private final ValueType value;

    private Tuple(KeyType key, ValueType value) {
        this.key = key;
        this.value = value;
    }

    public static Tuple<String, String> create(String key, String value) {
        return new Tuple<>(key, value);
    }

}
