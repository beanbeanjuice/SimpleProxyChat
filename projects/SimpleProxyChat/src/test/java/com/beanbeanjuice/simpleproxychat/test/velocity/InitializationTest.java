package com.beanbeanjuice.simpleproxychat.test.velocity;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class InitializationTest {

    private Player player;

    @BeforeEach
    public void setUp() {
        player = Mockito.mock(Player.class);
    }

    @Test
    @DisplayName("Should correctly verify that Mockito is setup for Velocity.")
    public void testInitialization() {
        player.sendMessage(Component.text("Hello, world!"));

        Mockito.verify(player).sendMessage(Mockito.argThat(
                (TextComponent component) -> component.content().equals("Hello, world!")
        ));
    }
}
