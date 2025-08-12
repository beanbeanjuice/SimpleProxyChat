package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.stream.Stream;

public class VelocityChatToggleCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityChatToggleCommand(final SimpleProxyChatVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource sender = invocation.source();

        if (args.length < 1 || args.length > 2) {
            executeError(sender);
            return;
        }

        if (args[0].equalsIgnoreCase("all")) {
            if (args.length != 2) {
                executeError(sender);
                return;
            }

            executeAll(sender, args[1]);
            return;
        }

        executeSingle(sender, args[0]);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        int length = invocation.arguments().length;
        if (length == 0) return List.of("all", "lock", "unlock");
        else if (length == 2) return List.of("lock", "unlock");
        else return List.of();
    }

    private void executeAll(CommandSource sender, String type) {
        if (!sender.hasPermission(Permission.COMMAND_TOGGLE_CHAT_ALL.getPermissionNode()) && sender instanceof Player) {
            String message = config.get(ConfigKey.MINECRAFT_COMMAND_NO_PERMISSION).asString();
            message = CommonHelper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
            );
            sender.sendMessage(Helper.stringToComponent(message));
            return;
        }

        Stream<String> servers = plugin.getProxyServer().getAllServers().stream()
                .map((registeredServer -> registeredServer.getServerInfo().getName()));

        switch (type.toLowerCase()) {
            case "lock" -> {
                servers.forEach((serverName) -> config.getServerChatLockHelper().addServer(serverName));
                String message = config.get(ConfigKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_LOCKED).asString();
                message = CommonHelper.replaceKeys(
                        message,
                        Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
                );
                sender.sendMessage(Helper.stringToComponent(message));
            }
            case "unlock" -> {
                servers.forEach((serverName) -> config.getServerChatLockHelper().removeServer(serverName));
                String message = config.get(ConfigKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_UNLOCKED).asString();
                message = CommonHelper.replaceKeys(
                        message,
                        Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
                );
                sender.sendMessage(Helper.stringToComponent(message));
            }
            default -> executeError(sender);
        }
    }

    private void executeSingle(CommandSource sender, String type) {
        if (!sender.hasPermission(Permission.COMMAND_TOGGLE_CHAT.getPermissionNode()) && sender instanceof Player) {
            String message = config.get(ConfigKey.MINECRAFT_COMMAND_NO_PERMISSION).asString();
            message = CommonHelper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
            );
            sender.sendMessage(Helper.stringToComponent(message));
            return;
        }

        if (!(sender instanceof Player player)) {
            String message = config.get(ConfigKey.MINECRAFT_COMMAND_MUST_BE_PLAYER).asString();
            message = CommonHelper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
            );
            sender.sendMessage(Helper.stringToComponent(message));
            return;
        }


        player.getCurrentServer().ifPresent((serverConnection -> {
            String currentServerName = serverConnection.getServerInfo().getName();
            Stream<String> servers = plugin.getProxyServer().getAllServers().stream()
                    .map(registeredServer -> registeredServer.getServerInfo().getName())
                    .filter(serverName -> serverName.equals(currentServerName));

            switch (type.toLowerCase()) {
                case "lock" -> {
                    servers.forEach((serverName) -> config.getServerChatLockHelper().addServer(serverName));
                    String message = config.get(ConfigKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_LOCKED).asString();
                    message = CommonHelper.replaceKeys(
                            message,
                            Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()),
                            Tuple.of("server", currentServerName)
                    );
                    sender.sendMessage(Helper.stringToComponent(message));
                }
                case "unlock" -> {
                    servers.forEach((serverName) -> config.getServerChatLockHelper().removeServer(serverName));
                    String message = config.get(ConfigKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_UNLOCKED).asString();
                    message = CommonHelper.replaceKeys(
                            message,
                            Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString()),
                            Tuple.of("server", currentServerName)
                    );
                    sender.sendMessage(Helper.stringToComponent(message));
                }
                default -> executeError(sender);
            }
        }));


    }

    private void executeError(CommandSource sender) {
        String message = config.get(ConfigKey.MINECRAFT_COMMAND_CHAT_LOCK_USAGE).asString();
        message = CommonHelper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.get(ConfigKey.PLUGIN_PREFIX).asString())
        );
        sender.sendMessage(Helper.stringToComponent(message));
    }

}
