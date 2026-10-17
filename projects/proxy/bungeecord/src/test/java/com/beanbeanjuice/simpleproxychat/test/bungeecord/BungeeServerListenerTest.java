package com.beanbeanjuice.simpleproxychat.test.bungeecord;

import com.beanbeanjuice.simpleproxychat.listeners.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.shared.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigValueWrapper;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BungeeServerListenerTest {

    private ISimpleProxyChat plugin;
    private Config config;

    @BeforeEach
    public void setUp() {
        config = Mockito.mock(Config.class);
        plugin = Mockito.mock(ISimpleProxyChat.class);
        when(plugin.getSPCConfig()).thenReturn(config);
        when(config.get(ConfigKey.DISABLED_SERVERS)).thenReturn(new ConfigValueWrapper(List.of("disabled-server")));
    }

    private ProxiedPlayer mockPlayerOnServer(String serverName) {
        ProxiedPlayer player = Mockito.mock(ProxiedPlayer.class);
        Server server = Mockito.mock(Server.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        when(serverInfo.getName()).thenReturn(serverName);
        when(server.getInfo()).thenReturn(serverInfo);
        when(player.getServer()).thenReturn(server);
        return player;
    }

    @Test
    @DisplayName("playerIsInDisabledServer returns true for a listed server (exact case).")
    public void testPlayerOnDisabledServerExactCase() {
        ProxiedPlayer player = mockPlayerOnServer("disabled-server");
        assertTrue(BungeeServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer returns true for a listed server (different case).")
    public void testPlayerOnDisabledServerDifferentCase() {
        ProxiedPlayer player = mockPlayerOnServer("DISABLED-SERVER");
        assertTrue(BungeeServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer returns false for an unlisted server.")
    public void testPlayerOnAllowedServer() {
        ProxiedPlayer player = mockPlayerOnServer("allowed-server");
        assertFalse(BungeeServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer returns false when player.getServer() is null.")
    public void testPlayerWithNullServer() {
        ProxiedPlayer player = Mockito.mock(ProxiedPlayer.class);
        when(player.getServer()).thenReturn(null);
        assertFalse(BungeeServerListener.playerIsInDisabledServer(player, plugin));
    }

}
