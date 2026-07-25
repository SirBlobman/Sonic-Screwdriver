package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class ItemConfiguration {
    private final SonicPlugin plugin;

    private String materialName;
    private String customData;
    private String displayNameString;
    private List<String> loreStrings;

    private transient Material material;
    private transient Component displayName;
    private transient List<Component> lore;

    private transient ItemStack item;

    public ItemConfiguration(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    public void load(@NotNull ConfigurationSection config) {
        setMaterialName(config.getString("material", "BLAZE_ROD"));
        setCustomData(config.getString("custom-model-data", "sonic.screwdriver"));
        setDisplayNameString(config.getString("display-name", "Sonic Screwdriver"));
        setLoreStrings(config.getStringList("lore"));
    }

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull ItemStack getItem() {
        if (this.item != null) {
            return this.item.clone();
        }

        Material material = getMaterial();
        ItemStack item = new ItemStack(material, 1);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(getDisplayName());
        itemMeta.lore(getLore());

        CustomModelDataComponent customModelDataComponent = itemMeta.getCustomModelDataComponent();
        customModelDataComponent.setStrings(List.of(getCustomData()));
        itemMeta.setCustomModelDataComponent(customModelDataComponent);
        item.setItemMeta(itemMeta);
        this.item = item.clone();
        return this.item;
    }

    public String getMaterialName() {
        return this.materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
        this.material = null;
    }

    public @NotNull Material getMaterial() {
        if (this.material != null) {
            return this.material;
        }

        String materialName = getMaterialName();
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            throw new InvalidConfigurationException("Unknown material name '" + materialName + "'.");
        }

        return (this.material = material);
    }

    public @NotNull String getCustomData() {
        return this.customData;
    }

    public void setCustomData(@NotNull String customData) {
        this.customData = customData;
    }

    public @Nullable String getDisplayNameString() {
        return this.displayNameString;
    }

    public void setDisplayNameString(@Nullable String displayNameString) {
        this.displayNameString = displayNameString;
        this.displayName = null;
    }

    public @Nullable Component getDisplayName() {
        if (this.displayName != null) {
            return this.displayName;
        }

        String displayNameString = getDisplayNameString();
        if (displayNameString == null) {
            return null;
        }

        MiniMessage miniMessage = getPlugin().getMiniMessage();
        this.displayName = miniMessage.deserialize(displayNameString);
        return this.displayName;
    }

    public @Nullable List<String> getLoreStrings() {
        return this.loreStrings;
    }

    public void setLoreStrings(@Nullable List<String> loreStrings) {
        this.loreStrings = loreStrings;
        this.lore = null;
    }

    public @Nullable List<Component> getLore() {
        if (this.lore != null) {
            return this.lore;
        }

        List<String> loreStrings = getLoreStrings();
        if (loreStrings == null) {
            return null;
        }

        MiniMessage miniMessage = getPlugin().getMiniMessage();
        List<Component> lore = new ArrayList<>();
        for (String loreString : loreStrings) {
            Component line = miniMessage.deserialize(loreString);
            lore.add(line);
        }

        return (this.lore = lore);
    }
}
