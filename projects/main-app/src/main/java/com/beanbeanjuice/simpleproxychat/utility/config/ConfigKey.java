package com.beanbeanjuice.simpleproxychat.utility.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ConfigKey {
    // CONFIG
    USE_DISCORD (ConfigFileType.CONFIG, "use-discord", Boolean.class),
    BOT_TOKEN (ConfigFileType.CONFIG, "BOT-TOKEN", String.class),
    CHANNEL_ID (ConfigFileType.CONFIG, "CHANNEL-ID", String.class),
    BOT_ACTIVITY_STATUS (ConfigFileType.CONFIG, "bot-activity.status", String.class),
    BOT_ACTIVITY_TYPE (ConfigFileType.CONFIG, "bot-activity.type", String.class),
    BOT_ACTIVITY_TEXT  (ConfigFileType.CONFIG, "bot-activity.text", String.class),
    SERVER_UPDATE_INTERVAL (ConfigFileType.CONFIG, "server-update-interval", Integer.class),
    ALIASES (ConfigFileType.CONFIG, "aliases", Map.class),
    USE_PERMISSIONS (ConfigFileType.CONFIG, "use-permissions", Boolean.class),
    PROXY_MESSAGE_PREFIX (ConfigFileType.CONFIG, "proxy-message-prefix", String.class),
    PROXY_MESSAGE_PREFIX_BLACKLIST (ConfigFileType.CONFIG, "proxy-message-prefix-blacklist", String.class),
    USE_INITIAL_SERVER_STATUS (ConfigFileType.CONFIG, "use-initial-server-status", Boolean.class),
    USE_FAKE_MESSAGES (ConfigFileType.CONFIG, "use-fake-messages", Boolean.class),
    TIMESTAMP_USE_API (ConfigFileType.CONFIG, "timestamp.use-api", Boolean.class),
    TIMESTAMP_FORMAT (ConfigFileType.CONFIG, "timestamp.format", String.class),
    TIMESTAMP_TIMEZONE (ConfigFileType.CONFIG, "timestamp.timezone", DateTimeZone.class),
    USE_HELPER (ConfigFileType.CONFIG, "use-helper", Boolean.class),
    UPDATE_NOTIFICATIONS (ConfigFileType.CONFIG, "update-notifications", Boolean.class),
    USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM (ConfigFileType.CONFIG, "use-simple-proxy-chat-banning-system", Boolean.class),
    SEND_PREVIOUS_MESSAGES_ON_SWITCH_ENABLED (ConfigFileType.CONFIG, "send-previous-messages-on-switch.enabled", Boolean.class),
    SEND_PREVIOUS_MESSAGES_ON_SWITCH_AMOUNT (ConfigFileType.CONFIG, "send-previous-messages-on-switch.amount", Integer.class),
    RELOAD_ALIASES (ConfigFileType.CONFIG, "commands.reload-aliases", List.class),
    CHAT_TOGGLE_ALIASES (ConfigFileType.CONFIG, "commands.chat-toggle-aliases", List.class),
    BAN_ALIASES (ConfigFileType.CONFIG, "commands.ban-aliases", List.class),
    UNBAN_ALIASES (ConfigFileType.CONFIG, "commands.unban-aliases", List.class),
    WHISPER_ALIASES (ConfigFileType.CONFIG, "commands.whisper-aliases", List.class),
    REPLY_ALIASES (ConfigFileType.CONFIG, "commands.reply-aliases", List.class),
    BROADCAST_ALIASES (ConfigFileType.CONFIG, "commands.broadcast-aliases", List.class),
    DISABLED_SERVERS (ConfigFileType.CONFIG, "disabled-servers", List.class),

    // MESSAGES
    PLUGIN_PREFIX (ConfigFileType.MESSAGES, "plugin-prefix", String.class),

    MINECRAFT_JOIN_ENABLED (ConfigFileType.MESSAGES, "minecraft.join.enabled", Boolean.class),
    MINECRAFT_JOIN (ConfigFileType.MESSAGES, "minecraft.join.message", String.class),
    MINECRAFT_LEAVE_ENABLED (ConfigFileType.MESSAGES, "minecraft.leave.enabled", Boolean.class),
    MINECRAFT_LEAVE (ConfigFileType.MESSAGES, "minecraft.leave.message", String.class),
    MINECRAFT_CHAT_ENABLED (ConfigFileType.MESSAGES, "minecraft.chat.enabled", Boolean.class),
    MINECRAFT_CHAT_MESSAGE (ConfigFileType.MESSAGES, "minecraft.chat.message", String.class),
    MINECRAFT_CHAT_VANISHED_MESSAGE (ConfigFileType.MESSAGES, "minecraft.chat.vanished", String.class),
    MINECRAFT_SWITCH_ENABLED (ConfigFileType.MESSAGES, "minecraft.switch.enabled", Boolean.class),
    MINECRAFT_SWITCH_DEFAULT (ConfigFileType.MESSAGES, "minecraft.switch.default", String.class),
    MINECRAFT_SWITCH_SHORT  (ConfigFileType.MESSAGES, "minecraft.switch.no-from", String.class),
    MINECRAFT_WHISPER_SEND (ConfigFileType.MESSAGES, "minecraft.whisper.send", String.class),
    MINECRAFT_WHISPER_RECEIVE (ConfigFileType.MESSAGES, "minecraft.whisper.receive", String.class),
    MINECRAFT_WHISPER_ERROR (ConfigFileType.MESSAGES, "minecraft.whisper.error", String.class),
    MINECRAFT_DISCORD_ENABLED (ConfigFileType.MESSAGES, "minecraft.discord.enabled", Boolean.class),
    MINECRAFT_DISCORD_MESSAGE (ConfigFileType.MESSAGES, "minecraft.discord.message", String.class),
    MINECRAFT_DISCORD_EMBED_USE (ConfigFileType.MESSAGES, "minecraft.discord.embed.use", Boolean.class),
    MINECRAFT_DISCORD_EMBED_TITLE (ConfigFileType.MESSAGES, "minecraft.discord.embed.title", String.class),
    MINECRAFT_DISCORD_EMBED_MESSAGE (ConfigFileType.MESSAGES, "minecraft.discord.embed.message", String.class),
    MINECRAFT_DISCORD_EMBED_COLOR (ConfigFileType.MESSAGES, "minecraft.discord.embed.color", Color.class),
    MINECRAFT_DISCORD_EMBED_USE_TIMESTAMP (ConfigFileType.MESSAGES, "minecraft.discord.embed.use-timestamp", Boolean.class),
    MINECRAFT_COMMAND_NO_PERMISSION (ConfigFileType.MESSAGES, "minecraft.command.no-permission", String.class),
    MINECRAFT_COMMAND_UNKNOWN (ConfigFileType.MESSAGES, "minecraft.command.unknown", String.class),
    MINECRAFT_COMMAND_MUST_BE_PLAYER (ConfigFileType.MESSAGES, "minecraft.command.must-be-player", String.class),
    MINECRAFT_COMMAND_RELOAD (ConfigFileType.MESSAGES, "minecraft.command.reload", String.class),
    MINECRAFT_COMMAND_CHAT_LOCK_USAGE (ConfigFileType.MESSAGES, "minecraft.command.chat-lock.usage", String.class),
    MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_LOCKED (ConfigFileType.MESSAGES, "minecraft.command.chat-lock.single.locked", String.class),
    MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_UNLOCKED (ConfigFileType.MESSAGES, "minecraft.command.chat-lock.single.unlocked", String.class),
    MINECRAFT_COMMAND_CHAT_LOCK_ALL_LOCKED (ConfigFileType.MESSAGES, "minecraft.command.chat-lock.all.locked", String.class),
    MINECRAFT_COMMAND_CHAT_LOCK_ALL_UNLOCKED (ConfigFileType.MESSAGES, "minecraft.command.chat-lock.all.unlocked", String.class),
    MINECRAFT_COMMAND_PROXY_BAN_USAGE (ConfigFileType.MESSAGES, "minecraft.command.proxy-ban.usage", String.class),
    MINECRAFT_COMMAND_PROXY_BAN_BANNED (ConfigFileType.MESSAGES, "minecraft.command.proxy-ban.banned", String.class),
    MINECRAFT_COMMAND_PROXY_BAN_UNBANNED (ConfigFileType.MESSAGES, "minecraft.command.proxy-ban.unbanned", String.class),
    MINECRAFT_COMMAND_PROXY_BAN_LOGIN_MESSAGE (ConfigFileType.MESSAGES, "minecraft.command.proxy-ban.login-message", String.class),
    MINECRAFT_COMMAND_BROADCAST_USAGE (ConfigFileType.MESSAGES, "minecraft.command.broadcast.usage", String.class),
    MINECRAFT_COMMAND_BROADCAST_MESSAGE (ConfigFileType.MESSAGES, "minecraft.command.broadcast.message", String.class),

    DISCORD_JOIN_ENABLED (ConfigFileType.MESSAGES, "discord.join.enabled", Boolean.class),
    DISCORD_JOIN_MESSAGE (ConfigFileType.MESSAGES, "discord.join.message", String.class),
    DISCORD_JOIN_USE_TIMESTAMP (ConfigFileType.MESSAGES, "discord.join.use-timestamp", Boolean.class),
    DISCORD_JOIN_USE_EMBED (ConfigFileType.MESSAGES, "discord.join.use-embed", Boolean.class),
    DISCORD_LEAVE_ENABLED (ConfigFileType.MESSAGES, "discord.leave.enabled", Boolean.class),
    DISCORD_LEAVE_MESSAGE (ConfigFileType.MESSAGES, "discord.leave.message", String.class),
    DISCORD_LEAVE_USE_TIMESTAMP (ConfigFileType.MESSAGES, "discord.leave.use-timestamp", Boolean.class),
    DISCORD_LEAVE_USE_EMBED (ConfigFileType.MESSAGES, "discord.leave.use-embed", Boolean.class),
    DISCORD_SWITCH_ENABLED (ConfigFileType.MESSAGES, "discord.switch.enabled", Boolean.class),
    DISCORD_SWITCH_MESSAGE (ConfigFileType.MESSAGES, "discord.switch.message", String.class),
    DISCORD_SWITCH_USE_TIMESTAMP (ConfigFileType.MESSAGES, "discord.switch.use-timestamp", Boolean.class),
    DISCORD_SWITCH_USE_EMBED (ConfigFileType.MESSAGES, "discord.switch.use-embed", Boolean.class),
    DISCORD_CHAT_ENABLED (ConfigFileType.MESSAGES, "discord.chat.enabled", Boolean.class),
    DISCORD_CHAT_MINECRAFT_MESSAGE (ConfigFileType.MESSAGES, "discord.chat.minecraft-message", String.class),
    DISCORD_TOPIC_ONLINE (ConfigFileType.MESSAGES, "discord.topic.online", String.class),
    DISCORD_TOPIC_OFFLINE  (ConfigFileType.MESSAGES, "discord.topic.offline", String.class),
    DISCORD_PROXY_STATUS_ENABLED (ConfigFileType.MESSAGES, "discord.proxy-status.enabled", Boolean.class),
    DISCORD_PROXY_STATUS_MODULE_ENABLED (ConfigFileType.MESSAGES, "discord.proxy-status.messages.enabled", String.class),
    DISCORD_PROXY_STATUS_MODULE_DISABLED (ConfigFileType.MESSAGES, "discord.proxy-status.messages.disabled", String.class),
    DISCORD_PROXY_STATUS_MODULE_TITLE (ConfigFileType.MESSAGES, "discord.proxy-status.messages.title", String.class),
    DISCORD_PROXY_STATUS_MODULE_MESSAGE (ConfigFileType.MESSAGES, "discord.proxy-status.messages.message", String.class),
    DISCORD_PROXY_STATUS_MODULE_ONLINE (ConfigFileType.MESSAGES, "discord.proxy-status.messages.online", String.class),
    DISCORD_PROXY_STATUS_MODULE_OFFLINE (ConfigFileType.MESSAGES, "discord.proxy-status.messages.offline", String.class),
    DISCORD_PROXY_STATUS_MODULE_USE_TIMESTAMP (ConfigFileType.MESSAGES, "discord.proxy-status.messages.use-timestamp", Boolean.class),

    // CONSOLE STUFF
    CONSOLE_CHAT (ConfigFileType.MESSAGES, "console.chat", Boolean.class),
    CONSOLE_JOIN (ConfigFileType.MESSAGES, "console.join", Boolean.class),
    CONSOLE_LEAVE (ConfigFileType.MESSAGES, "console.leave", Boolean.class),
    CONSOLE_SWITCH (ConfigFileType.MESSAGES, "console.switch", Boolean.class),
    CONSOLE_DISCORD_CHAT (ConfigFileType.MESSAGES, "console.discord-chat", Boolean.class),
    CONSOLE_SERVER_STATUS (ConfigFileType.MESSAGES, "console.server-status", Boolean.class),

    UPDATE_MESSAGE (ConfigFileType.MESSAGES, "update-message", String.class);

    private final ConfigFileType file;
    private final String key;
    private final Class<?> classType;

}
