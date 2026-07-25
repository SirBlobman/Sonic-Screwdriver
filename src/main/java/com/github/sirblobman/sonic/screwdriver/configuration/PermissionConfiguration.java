package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public final class PermissionConfiguration {
    private boolean enabled;
    private @NotNull String permissionName;
    private @NotNull String permissionDescription;
    private @NotNull String permissionDefaultName;

    private transient Permission permission;

    public PermissionConfiguration() {
        this.enabled = true;
        this.permissionName = "default.permission";
        this.permissionDescription = "Default Description";
        this.permissionDefaultName = PermissionDefault.OP.name();
    }

    public void load(@NotNull ConfigurationSection config) {
        setEnabled(config.getBoolean("enabled", true));
        setPermissionDefaultName(config.getString("name", "default.permission"));
        setPermissionDescription(config.getString("description", "Default Description"));
        setPermissionDefaultName(config.getString("default", PermissionDefault.OP.name()));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public @NotNull String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(@NotNull String permissionName) {
        this.permissionName = permissionName;
        this.permission = null;
    }

    public @NotNull String getPermissionDescription() {
        return this.permissionDescription;
    }

    public void setPermissionDescription(@NotNull String permissionDescription) {
        this.permissionDescription = permissionDescription;
        this.permission = null;
    }

    public @NotNull String getPermissionDefaultName() {
        return this.permissionDefaultName;
    }

    public void setPermissionDefaultName(@NotNull String permissionDefaultName) {
        this.permissionDefaultName = permissionDefaultName;
        this.permission = null;
    }

    public @NotNull Permission getPermission() {
        if (this.permission != null) {
            return this.permission;
        }

        String permissionName = getPermissionName();
        String permissionDescription = getPermissionDescription();
        String permissionDefaultName = getPermissionDefaultName().toUpperCase(Locale.US);
        PermissionDefault permissionDefault;

        try {
            permissionDefault = PermissionDefault.valueOf(permissionDefaultName);
        } catch (IllegalArgumentException ex) {
            throw new InvalidConfigurationException("Unknown PermissionDefault value '" + permissionDefaultName + "'.");
        }

        return (this.permission = new Permission(permissionName, permissionDescription, permissionDefault));
    }

    public boolean hasPermission(@NotNull Permissible permissible) {
        if (isEnabled()) {
            Permission permission = getPermission();
            return permissible.hasPermission(permission);
        }

        return true;
    }
}
