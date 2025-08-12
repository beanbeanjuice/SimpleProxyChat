package com.beanbeanjuice.simpleproxychat.shared.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigFileType {

    CONFIG ("config.yml"),
    MESSAGES ("messages.yml");

    private final String fileName;

}
