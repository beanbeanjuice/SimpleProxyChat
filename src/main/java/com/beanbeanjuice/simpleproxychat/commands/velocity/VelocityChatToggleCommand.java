package com.beanbeanjuice.simpleproxychat.commands.velocity;

import com.beanbeanjuice.simpleproxychat.SimpleProxyChatVelocity;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.stream.Stream;

public class VelocityChatToggleCommand implements SimpleCommand {

    private final SimpleProxyChatVelocity plugin;
    private final Config config;

    public VelocityChatToggleCommand(SimpleProxyChatVelocity plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
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
            String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_NO_PERMISSION);
            message = Helper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
            );
            sender.sendMessage(Helper.stringToComponent(message));
            return;
        }

        Stream<String> servers = plugin.getProxyServer().getAllServers().stream()
                .map((registeredServer -> registeredServer.getServerInfo().getName()));

        switch (type.toLowerCase()) {
            case "lock" -> {
                servers.forEach((serverName) -> config.getServerChatLockHelper().addServer(serverName));
                String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_LOCKED);
                message = Helper.replaceKeys(
                        message,
                        Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
                );
                sender.sendMessage(Helper.stringToComponent(message));
            }
            case "unlock" -> {
                servers.forEach((serverName) -> config.getServerChatLockHelper().removeServer(serverName));
                String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_ALL_UNLOCKED);
                message = Helper.replaceKeys(
                        message,
                        Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
                );
                sender.sendMessage(Helper.stringToComponent(message));
            }
            default -> executeError(sender);
        }
    }

    private void executeSingle(CommandSource sender, String type) {
        if (!sender.hasPermission(Permission.COMMAND_TOGGLE_CHAT.getPermissionNode()) && sender instanceof Player) {
            String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_NO_PERMISSION);
            message = Helper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
            );
            sender.sendMessage(Helper.stringToComponent(message));
            return;
        }

        if (!(sender instanceof Player player)) {
            String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_MUST_BE_PLAYER);
            message = Helper.replaceKeys(
                    message,
                    Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
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
                    String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_LOCKED);
                    message = Helper.replaceKeys(
                            message,
                            Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                            Tuple.of("server", currentServerName)
                    );
                    sender.sendMessage(Helper.stringToComponent(message));
                }
                case "unlock" -> {
                    servers.forEach((serverName) -> config.getServerChatLockHelper().removeServer(serverName));
                    String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_SINGLE_UNLOCKED);
                    message = Helper.replaceKeys(
                            message,
                            Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                            Tuple.of("server", currentServerName)
                    );
                    sender.sendMessage(Helper.stringToComponent(message));
                }
                default -> executeError(sender);
            }
        }));


    }

    private void executeError(CommandSource sender) {
        String message = config.getAsString(ConfigDataKey.MINECRAFT_COMMAND_CHAT_LOCK_USAGE);
        message = Helper.replaceKeys(
                message,
                Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX))
        );
        sender.sendMessage(Helper.stringToComponent(message));
    }

}
