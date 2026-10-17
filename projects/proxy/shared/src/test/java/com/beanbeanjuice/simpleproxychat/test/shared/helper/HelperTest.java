package com.beanbeanjuice.simpleproxychat.test.shared.helper;

import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigValueWrapper;
import com.beanbeanjuice.simpleproxychat.shared.helper.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HelperTest {

    private Config config;

    @BeforeEach
    public void setUp() {
        config = Mockito.mock(Config.class);
        when(config.get(ConfigKey.DISABLED_SERVERS)).thenReturn(new ConfigValueWrapper(List.of("disabled-server", "AnotherServer")));
    }

    @Test
    @DisplayName("Listed server (exact match) returns true.")
    public void testListedServerExactMatch() {
        assertTrue(Helper.isServerDisabled(config, "disabled-server"));
    }

    @Test
    @DisplayName("Listed server (different case) returns true.")
    public void testListedServerDifferentCase() {
        assertTrue(Helper.isServerDisabled(config, "DISABLED-SERVER"));
    }

    @Test
    @DisplayName("Unlisted server returns false.")
    public void testUnlistedServer() {
        assertFalse(Helper.isServerDisabled(config, "allowed-server"));
    }

    @Test
    @DisplayName("Null server name returns false.")
    public void testNullServerName() {
        assertFalse(Helper.isServerDisabled(config, null));
    }

}
