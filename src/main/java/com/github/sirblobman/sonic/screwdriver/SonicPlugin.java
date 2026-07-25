package com.github.sirblobman.sonic.screwdriver;

import org.jetbrains.annotations.NotNull;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import com.github.sirblobman.sonic.screwdriver.command.CommandSonicScrewdriver;
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;

public final class SonicPlugin extends JavaPlugin {
    private final SonicConfiguration configuration;
    private final NamespacedKey itemKey;
    private MiniMessage miniMessage;

    public SonicPlugin() {
        this.configuration = new SonicConfiguration(this);
        this.itemKey = new NamespacedKey(this, "sonic_screwdriver_item");
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        this.miniMessage = MiniMessage.miniMessage();
        reloadConfiguration();
        registerCommands();
        registerListeners();
        register_bStats();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    public @NotNull MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    public @NotNull SonicConfiguration getConfiguration() {
        return this.configuration;
    }

    public @NotNull NamespacedKey getItemKey() {
        return this.itemKey;
    }

    public void reloadConfiguration() {
        reloadConfig();
        FileConfiguration config = getConfig();
        getConfiguration().load(config);
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, registry -> {
            Commands registrar = registry.registrar();
            new CommandSonicScrewdriver(this).register(registrar);
        });
    }

    private void registerListeners() {
        new SonicListener(this).register();
    }

    private void register_bStats() {
        int pluginId = 16256; // https://bstats.org/plugin/bukkit/Sonic-Screwdriver/16256
        new Metrics(this, pluginId);
    }
}
