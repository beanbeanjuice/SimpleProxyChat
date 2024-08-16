package com.beanbeanjuice.simpleproxychat;

import com.beanbeanjuice.simpleproxychat.commands.bungee.*;
import com.beanbeanjuice.simpleproxychat.commands.bungee.ban.BungeeBanCommand;
import com.beanbeanjuice.simpleproxychat.commands.bungee.ban.BungeeUnbanCommand;
import com.beanbeanjuice.simpleproxychat.commands.bungee.whisper.BungeeReplyCommand;
import com.beanbeanjuice.simpleproxychat.commands.bungee.whisper.BungeeWhisperCommand;
import com.beanbeanjuice.simpleproxychat.socket.bungee.BungeeCordPluginMessagingListener;
import com.beanbeanjuice.simpleproxychat.utility.ISimpleProxyChat;
import com.beanbeanjuice.simpleproxychat.utility.helper.Helper;
import com.beanbeanjuice.simpleproxychat.utility.helper.WhisperHandler;
import com.beanbeanjuice.simpleproxychat.utility.BanHelper;
import com.beanbeanjuice.simpleproxychat.utility.config.Permission;
import com.beanbeanjuice.simpleproxychat.utility.epoch.EpochHelper;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeServerListener;
import com.beanbeanjuice.simpleproxychat.utility.listeners.bungee.BungeeVanishListener;
import com.beanbeanjuice.simpleproxychat.chat.ChatHandler;
import com.beanbeanjuice.simpleproxychat.discord.Bot;
import com.beanbeanjuice.simpleproxychat.utility.UpdateChecker;
import com.beanbeanjuice.simpleproxychat.utility.config.Config;
import com.beanbeanjuice.simpleproxychat.utility.config.ConfigDataKey;
import com.beanbeanjuice.simpleproxychat.utility.status.ServerStatusManager;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import litebans.api.Database;
import lombok.Getter;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import nl.chimpgamer.networkmanager.api.NetworkManagerPlugin;
import nl.chimpgamer.networkmanager.api.NetworkManagerProvider;
import org.bstats.bungeecord.Metrics;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class SimpleProxyChatBungee extends Plugin implements ISimpleProxyChat {

    @Getter private Config config;
    @Getter private EpochHelper epochHelper;
    @Getter private Bot discordBot;
    @Getter private Metrics metrics;
    @Getter private BungeeServerListener serverListener;
    @Getter private WhisperHandler whisperHandler;
    @Getter private BanHelper banHelper;
    private PluginManager pluginManager;

    @Override
    public void onEnable() {
        this.getLogger().info("The plugin is starting.");

        this.config = new Config(this.getDataFolder());
        this.config.initialize();

        epochHelper = new EpochHelper(config);

        this.getLogger().info("Attempting to initialize Discord bot... (IF ENABLED)");
        discordBot = new Bot(this.config, this.getLogger()::warning, this::getOnlinePlayers, this::getMaxPlayers);

        this.getProxy().getScheduler().runAsync(this, () -> {
            try { discordBot.start(); }
            catch (Exception e) { getLogger().warning("There was an error starting the discord bot: " + e.getMessage()); }
        });

        registerListeners();
        hookPlugins();
        registerCommands();

        // Discord Topic/Status Updater
        this.getProxy().getScheduler().schedule(this, discordBot::channelUpdaterFunction, 1, 10, TimeUnit.MINUTES);
        this.getProxy().getScheduler().schedule(this, discordBot::updateActivity, 6, 6, TimeUnit.MINUTES);

        // Update Checker
        startUpdateChecker();

        // bStats Stuff
        this.getLogger().info("Starting bStats... (IF ENABLED)");
        this.metrics = new Metrics(this, 21146);

        startPluginMessaging();

        // Plugin has started.
        this.getLogger().log(Level.INFO, "The plugin has been started.");

        // Send Initial Server Status
        discordBot.addRunnableToQueue(() -> {
            this.getProxy().getScheduler().schedule(this, () -> {
                this.config.overwrite(ConfigDataKey.PLUGIN_STARTING, false);

                ServerStatusManager manager = serverListener.getServerStatusManager();
                manager.getAllStatusStrings().forEach((string) -> this.getLogger().info(string));

                if (!config.getAsBoolean(ConfigDataKey.USE_INITIAL_SERVER_STATUS)) return;
                if (!config.getAsBoolean(ConfigDataKey.DISCORD_PROXY_STATUS_ENABLED)) return;
                this.discordBot.sendMessageEmbed(manager.getAllStatusEmbed());
            }, config.getAsInteger(ConfigDataKey.SERVER_UPDATE_INTERVAL) * 2L, TimeUnit.SECONDS);
        });
    }

    private void startUpdateChecker() {
        String currentVersion = this.getDescription().getVersion();

        UpdateChecker updateChecker = new UpdateChecker(
                config,
                currentVersion,
                (message) -> {
                    if (!config.getAsBoolean(ConfigDataKey.UPDATE_NOTIFICATIONS)) return;

                    this.getLogger().info(Helper.sanitize(message));

                    Component minimessage = MiniMessage.miniMessage().deserialize(config.getAsString(ConfigDataKey.PLUGIN_PREFIX) + message);
                    this.getProxy().getPlayers()
                            .stream()
                            .filter((player) -> player.hasPermission(Permission.READ_UPDATE_NOTIFICATION.getPermissionNode()))
                            .forEach((player) -> player.sendMessage(ChatMessageType.CHAT, BungeeComponentSerializer.get().serialize(minimessage)));
                }
        );

        this.getProxy().getScheduler().schedule(this, updateChecker::checkUpdate, 0, 12, TimeUnit.HOURS);
    }

    private void hookPlugins() {
        this.pluginManager = this.getProxy().getPluginManager();

        // Enable vanish support.
        if (this.isVanishAPIEnabled()) {
            this.getLogger().log(Level.INFO, "PremiumVanish/SuperVanish support has been enabled.");
            this.getProxy().getPluginManager().registerListener(this, new BungeeVanishListener(serverListener, config));
        }

        // Registering LuckPerms support.
        if (this.isLuckPermsEnabled()) {
            getLogger().info("LuckPerms support has been enabled.");
        }

        // Registering LiteBans support.
        if (this.isLiteBansEnabled()) {
            getLogger().info("LiteBans support has been enabled.");
        }

        // Registering AdvancedBan support.
        if (this.isAdvancedBanEnabled()) {
            getLogger().info("AdvancedBan support has been enabled.");
        }

        // Registering NetworkManager support.
        if (this.isNetworkManagerEnabled()) {
            getLogger().info("NetworkManager support has been enabled.");
        }

        // Registering the Simple Ban System
        if (!this.isLiteBansEnabled() && !this.isAdvancedBanEnabled() && config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
            getLogger().info("LiteBans and AdvancedBan not found. Using the built-in banning system for SimpleProxyChat...");
            banHelper = new BanHelper(this.getDataFolder());
            banHelper.initialize();
        } else {
            config.overwrite(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM, false);
        }
    }

    private void registerListeners() {
        // Register Discord Listener
        ChatHandler chatHandler = new ChatHandler(this, epochHelper);

        serverListener = new BungeeServerListener(this, chatHandler);
        this.getProxy().getPluginManager().registerListener(this, serverListener);

        whisperHandler = new WhisperHandler();
    }

    private void registerCommands() {
        this.getProxy().getPluginManager().registerCommand(this, new BungeeReloadCommand(this, config.getAsArrayList(ConfigDataKey.RELOAD_ALIASES).toArray(new String[0])));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeChatToggleCommand(this, config.getAsArrayList(ConfigDataKey.CHAT_TOGGLE_ALIASES).toArray(new String[0])));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeWhisperCommand(this, config.getAsArrayList(ConfigDataKey.WHISPER_ALIASES).toArray(new String[0])));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeReplyCommand(this, config.getAsArrayList(ConfigDataKey.REPLY_ALIASES).toArray(new String[0])));
        this.getProxy().getPluginManager().registerCommand(this, new BungeeBroadcastCommand(this, config.getAsArrayList(ConfigDataKey.BROADCAST_ALIASES).toArray(new String[0])));

        // Only enable when needed.
        if (config.getAsBoolean(ConfigDataKey.USE_SIMPLE_PROXY_CHAT_BANNING_SYSTEM)) {
            this.getProxy().getPluginManager().registerCommand(this, new BungeeBanCommand(this, config.getAsArrayList(ConfigDataKey.BAN_ALIASES).toArray(new String[0])));
            this.getProxy().getPluginManager().registerCommand(this, new BungeeUnbanCommand(this, config.getAsArrayList(ConfigDataKey.UNBAN_ALIASES).toArray(new String[0])));
        }
    }

    private void startPluginMessaging() {
        this.getProxy().registerChannel("BungeeCord");
        this.getProxy().getPluginManager().registerListener(this, new BungeeCordPluginMessagingListener(this, serverListener));
    }

    private void stopPluginMessaging() {
        this.getProxy().unregisterChannel("BungeeCord");
    }

    private int getOnlinePlayers() {
        if (this.isVanishAPIEnabled())
            return (int) this.getProxy().getPlayers().stream()
                    .filter((player) -> !BungeeVanishAPI.isInvisible(player))
                    .count();

        return this.getProxy().getOnlineCount();
    }

    private int getMaxPlayers() {
        return this.getProxy().getConfig().getPlayerLimit();
    }

    @Override
    public void onDisable() {
        this.getLogger().info("The plugin is shutting down...");
        stopPluginMessaging();
        discordBot.stop();
    }

    @Override
    public boolean isLuckPermsEnabled() {
        return this.pluginManager.getPlugin("LuckPerms") != null;
    }

    @Override
    public Optional<?> getLuckPerms() {
        if (!this.isLuckPermsEnabled()) return Optional.empty();
        return Optional.of(LuckPermsProvider.get());
    }

    @Override
    public boolean isVanishAPIEnabled() {
        return this.pluginManager.getPlugin("PremiumVanish") != null || this.pluginManager.getPlugin("SuperVanish") != null;
    }

    @Override
    public boolean isLiteBansEnabled() {
        return this.pluginManager.getPlugin("LiteBans") != null;
    }

    @Override
    public Optional<?> getLiteBansDatabase() {
        if (!this.isLiteBansEnabled()) return Optional.empty();

        return Optional.of(Database.get());
    }

    @Override
    public boolean isAdvancedBanEnabled() {
        return this.pluginManager.getPlugin("AdvancedBan") != null;
    }

    @Override
    public Optional<?> getAdvancedBanUUIDManager() {
        if (!this.isAdvancedBanEnabled()) return Optional.empty();

        return Optional.ofNullable(UUIDManager.get());
    }

    @Override
    public Optional<?> getAdvancedBanPunishmentManager() {
        if (!this.isAdvancedBanEnabled()) return Optional.empty();

        return Optional.ofNullable(PunishmentManager.get());
    }

    @Override
    public boolean isNetworkManagerEnabled() {
        return this.pluginManager.getPlugin("NetworkManager") != null;
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
        this.getProxy().broadcast(Helper.convertToBungee(message));
    }

    @Override
    public void log(String message) {
        this.getLogger().info(Helper.sanitize(message));
    }

}
