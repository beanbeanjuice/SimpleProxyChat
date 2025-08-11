package bungeecord;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class InitializationTest {

    private ProxiedPlayer player;

    @BeforeEach
    void setUp() {
        player = Mockito.mock(ProxiedPlayer.class);
    }

    @Test
    void testPlayerCanSendMessage() {
        player.sendMessage(new TextComponent("Hello, world!"));

        Mockito.verify(player).sendMessage(Mockito.argThat(
                (BaseComponent component) -> component.toPlainText().equals("Hello, world!")
        ));
    }
}
