package com.beanbeanjuice.simpleproxychathelper;

import com.beanbeanjuice.simpleproxychat.common.CommonHelper;
import com.beanbeanjuice.simpleproxychat.common.CommonUpdateChecker;
import com.beanbeanjuice.simpleproxychat.common.Tuple;
import com.beanbeanjuice.simpleproxychathelper.config.Config;
import com.beanbeanjuice.simpleproxychathelper.config.ConfigKey;
import lombok.Getter;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleProxyChatHelper extends JavaPlugin {

    private Metrics metrics;
    @Getter private Config options;
    @Getter private static final String subChannel = "SimpleProxyChat";

    @Override
    public void onEnable() {
        options = new Config();

        this.getLogger().info("Casting hooks...");
        setupPlaceholderAPI();

        this.getLogger().info("Setting up plugin-messaging...");
        setupPluginMessaging();

        this.getLogger().info("The plugin has been enabled!");

        metrics = new Metrics(this, 22052);
        startUpdateChecker();
    }

    private void setupPlaceholderAPI() {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) return;

        this.getLogger().info("PlaceholderAPI support has been enabled!");
        options.setOption(ConfigKey.PLACEHOLDER_API_SUPPORT, true);
    }

    private void startUpdateChecker() {
        String currentVersion = this.getDescription().getVersion();

        CommonUpdateChecker updateChecker = new CommonUpdateChecker(
                currentVersion,
                116966
        );

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            updateChecker.getUpdate().ifPresent((latestVersion) -> {
                // TODO: Refactor this later.
                String message = String.format("There is an update! Current version is %s. The latest version is %s!", currentVersion, latestVersion);
                this.getLogger().info(message);
            });
        }, 0, 864000);  // 864000 ticks = 12 hours
    }

    private void setupPluginMessaging() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordPluginMessageListener(this));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "custom:spc");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "custom:spc", new BungeeCordPluginMessageListener(this));
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "custom:spc");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "custom:spc");
    }

}
