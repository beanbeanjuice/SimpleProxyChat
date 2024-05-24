package com.beanbeanjuice.simpleproxychathelper;

import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleProxyChatHelper extends JavaPlugin {

    @Getter private static Permission vaultPermissions = null;
    @Getter private static Chat vaultChat = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getLogger().info("Attempting to hook into Vault!");

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().warning("Vault not found! Disabling...");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        if (!setupChat() || !setupPermissions()) {
            this.getLogger().warning("Failed to hook into Vault! Disabling...");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.getLogger().info("Successfully hooked into Vault!");
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);

        try { vaultChat = rsp.getProvider(); } catch (NullPointerException e) {
            this.getLogger().warning("There was an error hooking into Vault chat. Did you forget to install a chat plugin?");
        }
        return vaultChat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);

        try { vaultPermissions = rsp.getProvider(); } catch (NullPointerException e) {
            this.getLogger().warning("There was an error hooking into Vault permissions. Did you forget to install a permissions plugin?");
        }

        return vaultPermissions != null;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
