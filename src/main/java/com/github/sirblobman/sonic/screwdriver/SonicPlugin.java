package com.github.sirblobman.sonic.screwdriver;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import com.github.sirblobman.sonic.screwdriver.command.CommandSonicScrewdriver;
import com.github.sirblobman.sonic.screwdriver.configuration.MessageConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;

public final class SonicPlugin extends JavaPlugin {
    private final MiniMessage miniMessage;
    private final SonicConfiguration configuration;
    private final MessageConfiguration messageConfiguration;
    private final NamespacedKey itemKey;
    private final NamespacedKey enhancedKey;

    public SonicPlugin() {
        this.miniMessage = MiniMessage.builder(MiniMessage.Preset.DEFAULT).strict(true).build();
        this.configuration = new SonicConfiguration(this);
        this.messageConfiguration = new MessageConfiguration(this);
        this.itemKey = new NamespacedKey(this, "item");
        this.enhancedKey = new NamespacedKey(this, "enhanced");
    }

    @Override
    public void onLoad() {
        saveResource("config.yml", false);
        saveResource("messages.yml", false);
    }

    @Override
    public void onEnable() {
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

    public @NotNull MessageConfiguration getMessageConfiguration() {
        return this.messageConfiguration;
    }

    public @NotNull NamespacedKey getItemKey() {
        return this.itemKey;
    }

    public @NotNull NamespacedKey getEnhancedKey() {
        return this.enhancedKey;
    }

    public void reloadConfiguration() {
        reloadConfig();
        FileConfiguration config = getConfig();
        getConfiguration().load(config);

        File dataFolder = getDataFolder();
        File messagesFile = new File(dataFolder, "messages.yml");
        FileConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        getMessageConfiguration().load(messagesConfig);
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
