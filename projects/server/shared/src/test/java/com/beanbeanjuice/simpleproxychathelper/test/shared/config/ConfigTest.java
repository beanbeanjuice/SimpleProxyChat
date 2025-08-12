package com.beanbeanjuice.simpleproxychathelper.test.shared.config;

import com.beanbeanjuice.simpleproxychathelper.shared.config.Config;
import com.beanbeanjuice.simpleproxychathelper.shared.config.ConfigKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfigTest {

    @Test
    @DisplayName("Config Has Correct Default Values")
    public void testConfigDefaultValues() {
        Config config = new Config();

        Assertions.assertFalse(config.getOption(ConfigKey.PLACEHOLDER_API_SUPPORT));
    }

    @Test
    @DisplayName("Can Override Default Config Value")
    public void testOverridingConfigValue(){
        Config config = new Config();
        config.setOption(ConfigKey.PLACEHOLDER_API_SUPPORT, true);

        Assertions.assertTrue(config.getOption(ConfigKey.PLACEHOLDER_API_SUPPORT));
    }

}
