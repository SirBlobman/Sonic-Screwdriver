package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.api.configuration.IConfigurable;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class SonicConfiguration implements IConfigurable {
    private String permissionName;
    private XMaterial itemMaterial;
    private int itemQuantity;
    private int itemDamage;
    private Integer itemCustomModelData;

    private transient Permission permission;

    public SonicConfiguration() {
        this.permissionName = "sonic.screwdriver";
        this.itemMaterial = XMaterial.BLAZE_ROD;
        this.itemQuantity = 1;
        this.itemDamage = 0;
        this.itemCustomModelData = null;

        this.permission = null;
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setPermissionName(config.getString("permission", "sonic.screwdriver"));

        ConfigurationSection itemSection = getOrCreateSection(config, "item");
        String materialName = itemSection.getString("item.material", "BLAZE_ROD");
        Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
        setItemMaterial(optionalMaterial.orElse(XMaterial.BLAZE_ROD));

        setItemQuantity(itemSection.getInt("quantity", 1));
        setItemDamage(itemSection.getInt("damage", 0));

        if (itemSection.isInt("custom-model-data")) {
            setItemCustomModelData(itemSection.getInt("custom-model-data"));
        } else {
            setItemCustomModelData(null);
        }
    }

    public @Nullable String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(@Nullable String permissionName) {
        this.permissionName = permissionName;
        this.permission = null;
    }

    public @Nullable Permission getPermission() {
        if (this.permission != null) {
            return this.permission;
        }

        String permissionName = getPermissionName();
        if (permissionName == null || permissionName.isBlank()) {
            return null;
        }

        String description = "Use Sonic Screwdriver Item";
        return (this.permission = new Permission(permissionName, description, PermissionDefault.FALSE));
    }

    public @NotNull XMaterial getItemMaterial() {
        return this.itemMaterial;
    }

    public void setItemMaterial(@NotNull XMaterial itemMaterial) {
        this.itemMaterial = itemMaterial;
    }

    public int getItemQuantity() {
        return this.itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public int getItemDamage() {
        return this.itemDamage;
    }

    public void setItemDamage(int itemDamage) {
        this.itemDamage = itemDamage;
    }

    public @Nullable Integer getItemCustomModelData() {
        return this.itemCustomModelData;
    }

    public void setItemCustomModelData(@Nullable Integer itemCustomModelData) {
        this.itemCustomModelData = itemCustomModelData;
    }
}
