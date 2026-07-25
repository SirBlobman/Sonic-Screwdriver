package com.github.sirblobman.sonic.screwdriver.configuration;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.NumberConversions;

public final class ConfigurationHelper {
    public static float getFloat(@NotNull ConfigurationSection config, @NotNull String path, float defaultValue) {
        Object object = config.get(path, defaultValue);
        if (object instanceof Number) {
            return NumberConversions.toFloat(object);
        }

        return defaultValue;
    }
}
