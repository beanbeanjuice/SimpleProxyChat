package com.beanbeanjuice.simpleproxychat.test.shared.chat;

import com.beanbeanjuice.simpleproxychat.shared.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.shared.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.shared.config.Config;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.shared.config.ConfigValueWrapper;
import com.beanbeanjuice.simpleproxychat.shared.discord.Bot;
import com.beanbeanjuice.simpleproxychat.shared.helper.ServerChatLockHelper;
import com.beanbeanjuice.simpleproxychat.shared.socket.ChatMessageData;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ChatHandlerTest {

    private Config config;
    private Bot bot;
    private ISimpleProxyChat plugin;
    private ChatHandler chatHandler;

    @BeforeEach
    public void setUp() {
        config = Mockito.mock(Config.class);
        bot = Mockito.mock(Bot.class);
        plugin = Mockito.mock(ISimpleProxyChat.class);

        when(plugin.getSPCConfig()).thenReturn(config);
        when(plugin.getDiscordBot()).thenReturn(bot);
        when(plugin.isNetworkManagerEnabled()).thenReturn(false);
        when(plugin.isLuckPermsEnabled()).thenReturn(false);
        when(plugin.getHelperTcpServer()).thenReturn(Optional.empty());

        when(config.get(ConfigKey.DISABLED_SERVERS)).thenReturn(new ConfigValueWrapper(List.of("disabled-server")));
        when(config.get(ConfigKey.SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT)).thenReturn(new ConfigValueWrapper(10));
        when(config.getServerChatLockHelper()).thenReturn(new ServerChatLockHelper());

        chatHandler = new ChatHandler(plugin);
        clearInvocations(bot);
    }

    @Test
    @DisplayName("chat() with a disabled server sends nothing to Discord.")
    public void testChatDisabledServerSendsNothingToDiscord() {
        ChatMessageData data = Mockito.mock(ChatMessageData.class);
        when(data.getServername()).thenReturn("disabled-server");

        chatHandler.chat(data, "msg", "discord-msg", "title", "embed-msg");

        verifyNoInteractions(bot);
    }

    @Test
    @DisplayName("runProxyChatMessage() with a disabled server sends nothing to Discord.")
    public void testRunProxyChatMessageDisabledServerSendsNothingToDiscord() {
        ChatMessageData data = Mockito.mock(ChatMessageData.class);
        when(data.getServername()).thenReturn("disabled-server");

        chatHandler.runProxyChatMessage(data);

        verifyNoInteractions(bot);
    }

    @Test
    @DisplayName("chat() with an allowed server sends a message to Discord.")
    public void testChatAllowedServerSendsToDiscord() {
        when(config.get(ConfigKey.CONSOLE_CHAT)).thenReturn(new ConfigValueWrapper(false));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_ENABLED)).thenReturn(new ConfigValueWrapper(true));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_USE)).thenReturn(new ConfigValueWrapper(false));
        when(config.get(ConfigKey.MINECRAFT_CHAT_ENABLED)).thenReturn(new ConfigValueWrapper(false));

        ChatMessageData data = Mockito.mock(ChatMessageData.class);
        when(data.getServername()).thenReturn("allowed-server");

        chatHandler.chat(data, "msg", "discord-msg", "title", "embed-msg");

        verify(bot).sendMessage("discord-msg");
    }

    @Test
    @DisplayName("runProxyChatMessage() with an allowed server sends a message to Discord.")
    public void testRunProxyChatMessageAllowedServerSendsToDiscord() {
        when(config.get(ConfigKey.PROXY_MESSAGE_PREFIX)).thenReturn(new ConfigValueWrapper(""));
        when(config.get(ConfigKey.MINECRAFT_CHAT_MESSAGE)).thenReturn(new ConfigValueWrapper("{player}: {message}"));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_MESSAGE)).thenReturn(new ConfigValueWrapper("{player}: {message}"));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_TITLE)).thenReturn(new ConfigValueWrapper("title"));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_MESSAGE)).thenReturn(new ConfigValueWrapper("embed-msg"));
        when(config.get(ConfigKey.ALIASES)).thenReturn(new ConfigValueWrapper(new HashMap<String, String>()));
        when(config.get(ConfigKey.PLUGIN_PREFIX)).thenReturn(new ConfigValueWrapper("[SPC]"));
        when(config.get(ConfigKey.TIMESTAMP_TIMEZONE)).thenReturn(new ConfigValueWrapper(DateTimeZone.UTC));
        when(config.get(ConfigKey.TIMESTAMP_FORMAT)).thenReturn(new ConfigValueWrapper("HH:mm:ss"));
        when(config.get(ConfigKey.USE_WEBSOCKET)).thenReturn(new ConfigValueWrapper(false));
        when(config.get(ConfigKey.CONSOLE_CHAT)).thenReturn(new ConfigValueWrapper(false));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_ENABLED)).thenReturn(new ConfigValueWrapper(true));
        when(config.get(ConfigKey.MINECRAFT_DISCORD_EMBED_USE)).thenReturn(new ConfigValueWrapper(false));
        when(config.get(ConfigKey.MINECRAFT_CHAT_ENABLED)).thenReturn(new ConfigValueWrapper(false));

        ChatMessageData data = Mockito.mock(ChatMessageData.class);
        when(data.getServername()).thenReturn("allowed-server");
        when(data.getPlayerUUID()).thenReturn(UUID.randomUUID());
        when(data.getMessage()).thenReturn("hello");
        when(data.getPlayerName()).thenReturn("TestPlayer");

        chatHandler.runProxyChatMessage(data);

        verify(bot).sendMessage(anyString());
    }

}
