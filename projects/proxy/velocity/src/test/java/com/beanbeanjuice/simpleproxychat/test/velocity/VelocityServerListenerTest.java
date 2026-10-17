package com.beanbeanjuice.simpleproxychat.test.velocity;

import com.beanbeanjuice.simpleproxychat.shared.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigValueWrapper;
import com.beanbeanjuice.simpleproxychat.utility.listeners.VelocityServerListener;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VelocityServerListenerTest {

    private ISimpleProxyChat plugin;
    private Config config;

    @BeforeEach
    public void setUp() {
        config = Mockito.mock(Config.class);
        plugin = Mockito.mock(ISimpleProxyChat.class);
        when(plugin.getSPCConfig()).thenReturn(config);
        when(config.get(ConfigKey.DISABLED_SERVERS)).thenReturn(new ConfigValueWrapper(List.of("disabled-server")));
    }

    private Player mockPlayerOnServer(String serverName) {
        Player player = Mockito.mock(Player.class);
        ServerConnection connection = Mockito.mock(ServerConnection.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        when(serverInfo.getName()).thenReturn(serverName);
        when(connection.getServerInfo()).thenReturn(serverInfo);
        when(player.getCurrentServer()).thenReturn(Optional.of(connection));
        return player;
    }

    @Test
    @DisplayName("playerIsInDisabledServer(Player) returns true for a listed server (exact case).")
    public void testPlayerOnDisabledServerExactCase() {
        Player player = mockPlayerOnServer("disabled-server");
        assertTrue(VelocityServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer(Player) returns true for a listed server (different case).")
    public void testPlayerOnDisabledServerDifferentCase() {
        Player player = mockPlayerOnServer("DISABLED-SERVER");
        assertTrue(VelocityServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer(Player) returns false for an unlisted server.")
    public void testPlayerOnAllowedServer() {
        Player player = mockPlayerOnServer("allowed-server");
        assertFalse(VelocityServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer(Player) returns false when player has no current server.")
    public void testPlayerWithNoCurrentServer() {
        Player player = Mockito.mock(Player.class);
        when(player.getCurrentServer()).thenReturn(Optional.empty());
        assertFalse(VelocityServerListener.playerIsInDisabledServer(player, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer(ServerInfo) returns true for a listed server (different case).")
    public void testServerInfoDisabledDifferentCase() {
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        when(serverInfo.getName()).thenReturn("DISABLED-SERVER");
        assertTrue(VelocityServerListener.playerIsInDisabledServer(serverInfo, plugin));
    }

    @Test
    @DisplayName("playerIsInDisabledServer(ServerInfo) returns false for an unlisted server.")
    public void testServerInfoAllowed() {
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        when(serverInfo.getName()).thenReturn("allowed-server");
        assertFalse(VelocityServerListener.playerIsInDisabledServer(serverInfo, plugin));
    }

}
