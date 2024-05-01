package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.velocity.VelocityReloadCommand;
import com.beanbeanjuice.simpleproxychat.utility.Tuple;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import com.google.inject.Inject;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.utility.listeners.velocity.VelocityServerListener;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.Helper;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataEntry;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
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
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class SimpleProxyChatVelocity {

    private final Metrics.Factory metricsFactory;

    @Getter private final ProxyServer proxyServer;
    @Getter private final Logger logger;
    @Getter private final Config config;
    @Getter private final EpochHelper epochHelper;
    @Getter private Bot discordBot;
    private Metrics metrics;
    private VelocityServerListener serverListener;

    @Inject
    public SimpleProxyChatVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.metricsFactory = metricsFactory;

        this.getLogger().info("The plugin is starting.");
        this.config = new Config(dataDirectory.toFile());
        this.config.initialize();

        epochHelper = new EpochHelper(config);

        this.proxyServer.getScheduler().buildTask(this, () -> {
            this.getLogger().info("Initializing discord bot.");
            try { discordBot = new Bot(this.config); }
            catch (Exception e) { logger.warn("There was an error starting the discord bot: " + e.getMessage()); }
            discordBot.getJDA().ifPresentOrElse((jda) -> { }, () -> this.getLogger().error("Discord logging is not enabled."));

            // Bot ready.
            discordBot.start();
        }).schedule();

        // Plugin enabled.
        this.getLogger().info("Plugin has been initialized.");
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyInitialization(ProxyInitializeEvent event) {
        hookPlugins();
        registerListeners();
        registerCommands();

        // Start Channel Topic Updater
        this.proxyServer.getScheduler()
                .buildTask(this, () -> {
                    int numPlayers = proxyServer.getPlayerCount();

                    if (config.getAsBoolean(ConfigDataKey.VANISH_ENABLED))
                        numPlayers = (int) proxyServer.getAllPlayers().stream()
                                .filter((player) -> !VelocityVanishAPI.isInvisible(player))
                                .count();

                    discordBot.channelUpdaterFunction(numPlayers);
                })
                .delay(5, TimeUnit.MINUTES)
                .repeat(5, TimeUnit.MINUTES)
                .schedule();

        // Start Update Checker
        this.proxyServer.getScheduler()
                .buildTask(
                        this,
                        () -> UpdateChecker.checkUpdate(
                                (spigotMCVersion) -> this.proxyServer.getPluginManager().getPlugin("simpleproxychat")
                                        .flatMap(
                                                pluginContainer -> pluginContainer.getDescription().getVersion()
                                        ).ifPresent((currentVersion) -> {
                                                    if (currentVersion.equalsIgnoreCase(spigotMCVersion)) return;

                                                    String message = Helper.replaceKeys(
                                                            config.getAsString(ConfigDataKey.UPDATE_MESSAGE),
                                                            Tuple.of("plugin-prefix", config.getAsString(ConfigDataKey.PLUGIN_PREFIX)),
                                                            Tuple.of("old", currentVersion),
                                                            Tuple.of("new", spigotMCVersion),
                                                            Tuple.of("link", "https://www.spigotmc.org/resources/115305/")
                                                    );

                                                    this.getLogger().info(Helper.sanitize(message));
                                                    this.proxyServer.getAllPlayers()
                                                            .stream()
                                                            .filter((player) -> player.hasPermission(Permission.READ_UPDATE_NOTIFICATION.getPermissionNode()))
                                                            .forEach((player) -> player.sendMessage(Helper.stringToComponent(config.getAsString(ConfigDataKey.PLUGIN_PREFIX) + message)));
                                                }
                                        )
                        )
                ).delay(0, TimeUnit.MINUTES).repeat(12, TimeUnit.HOURS).schedule();

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        int pluginId = 21147;
        this.metrics = metricsFactory.make(this, pluginId);

        // Plugin has started.
        this.getLogger().info("The plugin has been started.");

        // All Status
        this.getProxyServer().getScheduler().buildTask(this, () -> {
            this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, new ConfigDataEntry(false));

            ServerStatusManager manager = serverListener.getServerStatusManager();
            manager.getAllStatusStrings().forEach(this.getLogger()::info);

            if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
            discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
        }).delay(config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS).schedule();
    }

    private void hookPlugins() {
        PluginManager pm = this.proxyServer.getPluginManager();

        // Enable vanish support.
        if (pm.getPlugin("PremiumVanish").isPresent() || pm.getPlugin("SuperVanish").isPresent()) {
            this.config.overwrite(ConfigDataKey.VANISH_ENABLED, new ConfigDataEntry(true));
            this.getLogger().info("PremiumVanish/SuperVanish support has been enabled.");
        }

        // Registering LuckPerms support.
        if (pm.getPlugin("luckperms").isPresent()) {
            config.overwrite(ConfigDataKey.LUCKPERMS_ENABLED, new ConfigDataEntry(true));
            this.getLogger().info("LuckPerms support has been enabled.");
        }

        // Registering LiteBans support.
        if (pm.getPlugin("litebans").isPresent()) {
            config.overwrite(ConfigDataKey.LITEBANS_ENABLED, new ConfigDataEntry(true));
            this.getLogger().info("LiteBans support has been enabled.");
        }

        if (pm.getPlugin("networkmanager").isPresent()) {
            config.overwrite(ConfigDataKey.NETWORKMANAGER_ENABLED, new ConfigDataEntry(true));
            this.getLogger().info("NetworkManager support has been enabled.");
        }
    }

    private void registerListeners() {
        // Register Chat Listener
        ChatHandler chatHandler = new ChatHandler(
                config,
                epochHelper,
                discordBot,
                (message) -> {
                    logger.info(Helper.sanitize(message));
                    Component messageComponent = MiniMessage.miniMessage().deserialize(message);
                    proxyServer.getAllPlayers().forEach((player) -> player.sendMessage(messageComponent));
                },
                (message) -> logger.info(Helper.sanitize(message))
        );
        serverListener = new VelocityServerListener(this, chatHandler);
        this.proxyServer.getEventManager().register(this, serverListener);
    }

    private void registerCommands() {
        CommandManager commandManager = proxyServer.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("spc-reload")
                .aliases("spcreload")
                .plugin(this)
                .build();

        commandManager.register(commandMeta, new VelocityReloadCommand(this, config));
    }

    @Subscribe(order = PostOrder.LAST)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.getLogger().info("The plugin is shutting down...");
        discordBot.stop();
    }

}
