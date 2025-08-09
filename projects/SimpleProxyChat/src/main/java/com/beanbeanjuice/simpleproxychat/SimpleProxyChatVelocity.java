package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityBroadcastCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityChatToggleCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityReloadCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.whisper.VelocityReplyCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.whisper.VelocityWhisperCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.ban.VelocityBanCommand;
import com.beanbeanjuice.simpleproxychat.commands.velocity.ban.VelocityUnbanCommand;
import com.beanbeanjuice.simpleproxychat.socket.velocity.VelocityPluginMessagingListener;
import com.beanbeanjuice.simpleproxychat.utility.BanHelper;
import com.beanbeanjuice.simpleproxychat.utility.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.helper.WhisperHandler;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.google.inject.Inject;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.listeners.velocity.VelocityServerListener;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigKey;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import litebans.api.Database;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPermsProvider;
import nl.chimpgamer.networkmanager.api.NetworkManagerProvider;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SimpleProxyChatVelocity implements ISimpleProxyChat {

    private final Metrics.Factory metricsFactory;

    @Getter private boolean pluginStarting = true;

    @Getter private final ProxyServer proxyServer;
    @Getter private final Logger logger;
    @Getter private final Config config;
    @Getter private final EpochHelper epochHelper;
    @Getter private Bot discordBot;
    @Getter private WhisperHandler whisperHandler;
    @Getter private BanHelper banHelper;
    private Metrics metrics;
    private VelocityServerListener serverListener;

    private PluginManager pluginManager;

    private final File dataDirectory;

    @Inject
    public SimpleProxyChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.getLogger().info("The plugin is starting.");
        this.dataDirectory = dataDirectory.toFile();
        this.config = new Config(dataDirectory.toFile());
        this.config.initialize();

        epochHelper = new EpochHelper(config);

        // Plugin enabled.
        this.getLogger().info("Plugin has been initialized.");
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize discord bot.
        this.getLogger().info("Attempting to initialize Discord bot... (IF ENABLED)");
        discordBot = new Bot(this.config, this.getLogger()::warn, this::getOnlinePlayers, this::getMaxPlayers);

        // Bot ready.
        this.proxyServer.getScheduler().buildTask(this, () -> {
            try { discordBot.start(); }
            catch (Exception e) { this.getLogger().warn("There was an error starting the discord bot: {}", e.getMessage()); }
        }).schedule();

        hookPlugins();
        registerListeners();
        registerCommands();

        // Start Channel Topic Updater
        this.proxyServer.getScheduler().buildTask(this, discordBot::channelUpdaterFunction).delay(1, TimeUnit.MINUTES).repeat(10, TimeUnit.MINUTES).schedule();
        this.proxyServer.getScheduler().buildTask(this, discordBot::updateActivity).delay(6, TimeUnit.MINUTES).repeat(6, TimeUnit.MINUTES).schedule();

        // Start Update Checker
        startUpdateChecker();

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        this.metrics = metricsFactory.make(this, 21147);

        // Plugin has started.
        this.getLogger().info("The plugin has been started.");

        // Send initial server status.
        discordBot.addRunnableToQueue(() -> {
            this.getProxyServer().getScheduler().buildTask(this, () -> {
                this.pluginStarting = false;

                ServerStatusManager manager = serverListener.getServerStatusManager();
                manager.getAllStatusStrings().stream().map(Helper::sanitize).forEach(this.getLogger()::info);

                if (!config.get(ConfigKey.USE_INITIAL_SERVER_STATUS).asBoolean()) return;
                if (!config.get(ConfigKey.DISCORD_PROXY_STATUS_ENABLED).asBoolean()) return;
                discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
            })
            .delay(config.get(ConfigKey.SERVER_UPDATE_INTERVAL).asInt() * 2L, TimeUnit.SECONDS)
            .schedule();
        });
    }

    private void startUpdateChecker() {
        String currentVersion = this.proxyServer.getPluginManager().getPlugin("simpleproxychat")
                .flatMap(pluginContainer -> pluginContainer.getDescription().getVersion())
                .get();

        UpdateChecker updateChecker = new UpdateChecker(
                config,
                currentVersion,
                (message) -> {
                    if (!config.get(ConfigKey.UPDATE_NOTIFICATIONS).asBoolean()) return;
                    this.getLogger().info(Helper.sanitize(message));
                    this.proxyServer.getAllPlayers()
                            .stream()
                            .filter((player) -> player.hasPermission(Permission.READ_UPDATE_NOTIFICATION.getPermissionNode()))
                            .forEach((player) -> player.sendMessage(Helper.stringToComponent(config.get(ConfigKey.PLUGIN_PREFIX).asString() + message)));
                }
        );

        this.proxyServer.getScheduler().buildTask(this, updateChecker::checkUpdate)
                .delay(0, TimeUnit.MINUTES)
                .repeat(12, TimeUnit.HOURS)
                .schedule();
    }

    private void hookPlugins() {
        this.pluginManager = this.proxyServer.getPluginManager();

        // Enable vanish support.
        if (this.isVanishAPIEnabled()) {
            this.getLogger().info("PremiumVanish/SuperVanish support has been enabled.");
        }

        // Registering LuckPerms support.
        if (this.isLuckPermsEnabled()) {
            this.getLogger().info("LuckPerms support has been enabled.");
        }

        // Registering LiteBans support.
        if (this.isLiteBansEnabled()) {
            this.getLogger().info("LiteBans support has been enabled.");
        }

        // Registering AdvancedBan support.
        if (this.isAdvancedBanEnabled()) {
            this.getLogger().info("AdvancedBan support has been enabled.");
        }

        // Registering NetworkManager support.
        if (this.isNetworkManagerEnabled()) {
            this.getLogger().info("NetworkManager support has been enabled.");
        }

        // Registering the Simple Banning System
        if (!this.isLiteBansEnabled() && !this.isAdvancedBanEnabled() && config.get(ConfigKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM).asBoolean()) {
            getLogger().info("LiteBans and AdvancedBan not found. Using the built-in banning system for SimpleProxyChat...");
            banHelper = new BanHelper(dataDirectory);
            banHelper.initialize();
        } else {
            config.overwrite(ConfigKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM, false);
        }
    }

    private void registerListeners() {
        // Register chat listener.
        ChatHandler chatHandler = new ChatHandler(this, epochHelper);
        serverListener = new VelocityServerListener(this, chatHandler);
        serverListener.initializeVelocityVanishListener();
        this.proxyServer.getEventManager().register(this, serverListener);

        this.proxyServer.getEventManager().register(this, new VelocityPluginMessagingListener(this, serverListener));
        this.proxyServer.getChannelRegistrar().register(VelocityPluginMessagingListener.IDENTIFIER);

        whisperHandler = new WhisperHandler();
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();

        CommandMeta reloadCommand = commandManager.metaBuilder("spc-reload")
                .aliases(config.get(ConfigKey.RELOAD_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta chatToggleCommand = commandManager.metaBuilder("spc-chat")
                .aliases(config.get(ConfigKey.CHAT_TOGGLE_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta whisperCommand = commandManager.metaBuilder("spc-whisper")
                .aliases(config.get(ConfigKey.WHISPER_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta replyCommand = commandManager.metaBuilder("spc-reply")
                .aliases(config.get(ConfigKey.REPLY_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta banCommand = commandManager.metaBuilder("spc-ban")
                .aliases(config.get(ConfigKey.BAN_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta unbanCommand = commandManager.metaBuilder("spc-unban")
                .aliases(config.get(ConfigKey.UNBAN_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        CommandMeta broadcastCommand = commandManager.metaBuilder("spc-broadcast")
                .aliases(config.get(ConfigKey.BROADCAST_ALIASES).asList().toArray(new String[0]))
                .plugin(this)
                .build();

        commandManager.register(reloadCommand, new VelocityReloadCommand(this));
        commandManager.register(chatToggleCommand, new VelocityChatToggleCommand(this));
        commandManager.register(whisperCommand, new VelocityWhisperCommand(this));
        commandManager.register(replyCommand, new VelocityReplyCommand(this));
        commandManager.register(broadcastCommand, new VelocityBroadcastCommand(this));

        // Only enable if the Simple Banning System is enabled.
        if (config.get(ConfigKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM).asBoolean()) {
            commandManager.register(banCommand, new VelocityBanCommand(this));
            commandManager.register(unbanCommand, new VelocityUnbanCommand(this));
        }
    }

    private int getOnlinePlayers() {
        if (this.isVanishAPIEnabled())
            return (int) proxyServer.getAllPlayers().stream()
                    .filter((player) -> !VelocityVanishAPI.isInvisible(player))
                    .count();

        return this.getProxyServer().getPlayerCount();
    }

    private int getMaxPlayers() {
        return this.getProxyServer().getConfiguration().getShowMaxPlayers();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.getLogger().info("The plugin is shutting down...");
        discordBot.stop();
    }

    @Override
    public boolean isLuckPermsEnabled() {
        return this.pluginManager.getPlugin("luckperms").isPresent();
    }

    @Override
    public Optional<?> getLuckPerms() {
        if (!this.isLuckPermsEnabled()) return Optional.empty();

        return Optional.of(LuckPermsProvider.get());
    }

    @Override
    public boolean isVanishAPIEnabled() {
        return (pluginManager.getPlugin("premiumvanish").isPresent() || pluginManager.getPlugin("supervanish").isPresent());
    }

    @Override
    public boolean isLiteBansEnabled() {
        return this.pluginManager.getPlugin("litebans").isPresent();
    }

    @Override
    public Optional<?> getLiteBansDatabase() {
        if (!this.isLiteBansEnabled()) return Optional.empty();

        return Optional.ofNullable(Database.get());
    }

    @Override
    public boolean isAdvancedBanEnabled() {
        return this.pluginManager.getPlugin("advancedban").isPresent();
    }

    @Override
    public Optional<?> getAdvancedBanUUIDManager() {
        if (!this.isAdvancedBanEnabled()) return Optional.empty();

        return Optional.of(UUIDManager.get());
    }

    @Override
    public Optional<?> getAdvancedBanPunishmentManager() {
        if (!this.isAdvancedBanEnabled()) return Optional.empty();

        return Optional.of(PunishmentManager.get());
    }

    @Override
    public boolean isNetworkManagerEnabled() {
        return this.pluginManager.getPlugin("networkmanager").isPresent();
    }

    @Override
    public Optional<?> getNetworkManager() {
        if (!this.isNetworkManagerEnabled()) return Optional.empty();

        return Optional.of(NetworkManagerProvider.Companion.get());
    }

    @Override
    public Config getSPCConfig() {
        return this.config;
    }

    @Override
    public void sendAll(String message) {
        logger.info(Helper.sanitize(message));
        Component messageComponent = MiniMessage.miniMessage().deserialize(message);
        proxyServer.getAllPlayers().forEach((player) -> player.sendMessage(messageComponent));
    }

    @Override
    public void log(String message) {
        this.logger.info(Helper.sanitize(message));
    }
}
