package com.github.sirblobman.sonic.screwdriver.configuration;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;

public final class ConfigurationHelper {
    public static float getFloat(@NotNull ConfigurationSection config, @NotNull String path, float defaultValue) {
        Object object = config.get(path, defaultValue);
        if (object instanceof Number) {
            return NumberConversions.toFloat(object);
        }

        return defaultValue;
    }

    public static void saveResourceIfNotExists(@NotNull Plugin plugin, @NotNull String fileName) {
        File dataFolder = plugin.getDataFolder();
        File file = new File(dataFolder, fileName);
        if (file.exists()) {
            return;
        }

        plugin.saveResource(fileName, false);
    }
}
