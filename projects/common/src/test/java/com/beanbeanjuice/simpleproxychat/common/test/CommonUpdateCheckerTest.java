package com.beanbeanjuice.simpleproxychat.common.test;

import com.beanbeanjuice.simpleproxychat.common.CommonUpdateChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class CommonUpdateCheckerTest {

    private static final long SIMPLE_PROXY_CHAT_SPIGOTMC_ID = 115305;
    private static final long SIMPLE_PROXY_CHAT_HELPER_SPIGOTMC_ID = 116966;

    private CommonUpdateChecker proxyUpdateChecker;
    private CommonUpdateChecker helperUpdateChecker;

    @BeforeEach
    public void setUp() {
        this.proxyUpdateChecker = new CommonUpdateChecker(
                "0.0.0",
                SIMPLE_PROXY_CHAT_SPIGOTMC_ID
        );

        this.helperUpdateChecker = new CommonUpdateChecker(
                "0.0.0",
                SIMPLE_PROXY_CHAT_HELPER_SPIGOTMC_ID
        );
    }

    @Test
    @DisplayName("Can Get Spigot Version for SimpleProxyChat")
    public void testCanGetSpigotVersionForSimpleProxyChat() {
        Optional<String> update = this.proxyUpdateChecker.getUpdate();
        Assertions.assertTrue(update.isPresent());
        Assertions.assertFalse(update.get().isBlank());
    }

    @Test
    @DisplayName("Can Get Spigot Version for SimpleProxyChatHelper")
    public void testCanGetSpigotVersionForSimpleProxyChatHelper() {
        Optional<String> update = this.helperUpdateChecker.getUpdate();
        Assertions.assertTrue(update.isPresent());
        Assertions.assertFalse(update.get().isBlank());
    }

    @Test
    @DisplayName("Should Return Remote String")
    public void testShouldReturnRemoteString() {
        Optional<String> version = this.proxyUpdateChecker.getUpdate();
        Assertions.assertTrue(version.isPresent());
        Assertions.assertFalse(version.get().isBlank());
    }

    @Test
    @DisplayName("Should Not Return Remote String")
    public void testShouldNotReturnRemoteString() {
        CommonUpdateChecker updateChecker = new CommonUpdateChecker(
                "999.999.999",
                SIMPLE_PROXY_CHAT_SPIGOTMC_ID
        );

        Optional<String> version = updateChecker.getUpdate();
        Assertions.assertFalse(version.isPresent());
    }

}
