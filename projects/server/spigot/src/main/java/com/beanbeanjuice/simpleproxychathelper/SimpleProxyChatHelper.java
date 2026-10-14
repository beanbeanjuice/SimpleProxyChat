package com.beanbeanjuice.simpleproxychathelper;

import com.beanbeanjuice.simpleproxychat.common.CommonUpdateChecker;
import com.beanbeanjuice.simpleproxychathelper.listeners.SpigotChatListener;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleProxyChatHelper extends JavaPlugin {

    private Metrics metrics;
    @Getter private HelperTcpClient helperTcpClient;

    @Override
    public void onEnable() {
        ensureConfig();

        this.getLogger().info("Casting hooks...");
        boolean papiEnabled = setupPlaceholderAPI();

        this.getLogger().info("Setting up proxy TCP connection...");
        setupTcpClient(papiEnabled);

        this.getLogger().info("The plugin has been enabled!");

        metrics = new Metrics(this, 22052);
        startUpdateChecker();
    }

    @Override
    public void onDisable() {
        if (helperTcpClient != null) helperTcpClient.stop();
    }

    private boolean setupPlaceholderAPI() {
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) return false;

        this.getLogger().info("PlaceholderAPI support has been enabled!");
        return true;
    }

    private void setupTcpClient(boolean papiEnabled) {
        String host = getConfig().getString("proxy-host", "localhost");
        int port = getConfig().getInt("proxy-port", 8766);
        String secret = getConfig().getString("proxy-secret", "changeme");
        String serverName = getConfig().getString("server-name", "survival");

        helperTcpClient = new HelperTcpClient(host, port, secret, serverName, papiEnabled, this);
        helperTcpClient.start();

        Bukkit.getPluginManager().registerEvents(new SpigotChatListener(helperTcpClient), this);
    }

    // Copies helper-config.yml from the JAR to config.yml in the data folder on first run.
    // This is just to avoid collisions.
    private void ensureConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            reloadConfig();
            return;
        }
        getDataFolder().mkdirs();
        try (InputStream in = getResource("helper-config.yml");
             FileOutputStream out = new FileOutputStream(configFile)) {
            if (in != null) in.transferTo(out);
        } catch (IOException e) {
            getLogger().warning("Could not save default config: " + e.getMessage());
        }
        reloadConfig();
    }

    private void startUpdateChecker() {
        String currentVersion = this.getDescription().getVersion();

        CommonUpdateChecker updateChecker = new CommonUpdateChecker(currentVersion, 116966);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            updateChecker.getUpdate().ifPresent((latestVersion) -> {
                // TODO: Refactor this later.
                String message = String.format("There is an update! Current version is %s. The latest version is %s!", currentVersion, latestVersion);
                this.getLogger().info(message);
            });
        }, 0, 864000);  // 864000 ticks = 12 hours
    }

}
