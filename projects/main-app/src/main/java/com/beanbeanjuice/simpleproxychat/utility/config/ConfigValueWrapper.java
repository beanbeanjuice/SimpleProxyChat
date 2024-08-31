package com.beanbeanjuice.simpleproxychat.utility.config;

import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class ConfigValueWrapper {

    private final Object value;

    public String asString() {
        return (String) value;
    }

    public int asInt() {
        return (int) value;
    }

    public boolean asBoolean() {
        return (boolean) value;
    }

    public Color asColor() {
        return (Color) value;
    }

    public HashMap<String, String> asStringMap() {
        return (HashMap<String, String>) value;
    }

    public List<String> asList() {
        return (List<String>) value;
    }

    public DateTimeZone asDateTimeZone() {
        return (DateTimeZone) value;
    }

}
