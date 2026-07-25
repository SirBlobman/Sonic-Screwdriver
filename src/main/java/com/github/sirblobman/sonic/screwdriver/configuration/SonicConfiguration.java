package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;

import net.kyori.adventure.sound.Sound;

public final class SonicConfiguration {
    private final SonicPlugin plugin;
    private final ItemConfiguration sonicItem;

    private boolean debugMode;
    private String permissionName;
    private String permissionDescription;
    private String permissionDefaultName;

    private boolean abilityOpenDoors;
    private boolean abilityLightNetherPortal;
    private boolean abilityInstantBreakBlocks;
    private boolean abilityCreateOverpoweredTNT;

    private List<String> instantBreakBlockTypeNames;

    private String soundName;
    private String soundCategoryName;
    private float soundVolume;
    private float soundPitch;

    private transient Set<Material> instantBreakBlockTypes;
    private transient Permission permission;
    private transient Sound sound;

    public SonicConfiguration(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
        this.sonicItem = new ItemConfiguration(plugin);
    }

    public void load(@NotNull ConfigurationSection config) {
        setDebugMode(config.getBoolean("debug-mode", false));
        setPermissionName(config.getString("permission.name", "sonic.screwdriver"));
        setPermissionDescription(config.getString("permission.description", "Default Description"));
        setPermissionDefaultName(config.getString("permission.default", "OP"));

        ConfigurationSection item = config.getConfigurationSection("item");
        if (item == null) {
            throw new InvalidConfigurationException("Missing 'item' section in config.yml");
        }

        ItemConfiguration sonicItemConfiguration = getSonicItem();
        sonicItemConfiguration.load(item);

        setAbilityOpenDoors(config.getBoolean("abilities.open-doors", true));
        setAbilityLightNetherPortal(config.getBoolean("abilities.light-nether-portal", true));
        setAbilityInstantBreakBlocks(config.getBoolean("abilities.instant-break-blocks", true));
        setAbilityCreateOverpoweredTNT(config.getBoolean("abilities.create-overpowered-tnt", true));

        setInstantBreakBlockTypeNames(config.getStringList("instant-break-block-types"));

        setSoundName(config.getString("sound.sound", "sonic:sonic.screwdriver"));
        setSoundCategoryName(config.getString("sound.category", "player"));
        setSoundVolume((float) config.getDouble("sound.volume", 1.0D));
        setSoundPitch((float) config.getDouble("sound.pitch", 1.0D));
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
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

    public boolean hasAbilityOpenDoors() {
        return this.abilityOpenDoors;
    }

    public void setAbilityOpenDoors(boolean abilityOpenDoors) {
        this.abilityOpenDoors = abilityOpenDoors;
    }

    public boolean hasAbilityLightNetherPortal() {
        return this.abilityLightNetherPortal;
    }

    public void setAbilityLightNetherPortal(boolean abilityLightNetherPortal) {
        this.abilityLightNetherPortal = abilityLightNetherPortal;
    }

    public boolean hasAbilityInstantBreakBlocks() {
        return this.abilityInstantBreakBlocks;
    }

    public void setAbilityInstantBreakBlocks(boolean abilityInstantBreakBlocks) {
        this.abilityInstantBreakBlocks = abilityInstantBreakBlocks;
    }

    public boolean hasAbilityCreateOverpoweredTNT() {
        return this.abilityCreateOverpoweredTNT;
    }

    public void setAbilityCreateOverpoweredTNT(boolean abilityCreateOverpoweredTNT) {
        this.abilityCreateOverpoweredTNT = abilityCreateOverpoweredTNT;
    }

    public @NotNull ItemConfiguration getSonicItem() {
        return this.sonicItem;
    }

    public String getSoundName() {
        return this.soundName;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
        this.sound = null;
    }

    public String getSoundCategoryName() {
        return this.soundCategoryName;
    }

    public void setSoundCategoryName(String soundCategoryName) {
        this.soundCategoryName = soundCategoryName;
        this.sound = null;
    }

    public float getSoundVolume() {
        return this.soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    public float getSoundPitch() {
        return this.soundPitch;
    }

    public void setSoundPitch(float soundPitch) {
        this.soundPitch = soundPitch;
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

        Permission permission = new Permission(permissionName, permissionDescription, permissionDefault);
        return (this.permission = permission);
    }

    public @NotNull Sound getSound() {
        String soundKeyString = getSoundName();
        String soundCategoryName = getSoundCategoryName().toUpperCase(Locale.US);
        float volume = getSoundVolume();
        float pitch = getSoundPitch();

        NamespacedKey soundKey = NamespacedKey.fromString(soundKeyString);
        if (soundKey == null) {
            throw new InvalidConfigurationException("Invalid sound name key '" + soundKeyString + "'.");
        }

        Sound.Source source;
        try {
            source = Sound.Source.valueOf(soundCategoryName);
        } catch (IllegalArgumentException ex) {
            throw new InvalidConfigurationException("Unknown sound category value '" + soundCategoryName + "'.");
        }

        this.sound = Sound.sound(soundKey, source, volume, pitch);
        return this.sound;
    }

    public @NotNull @UnmodifiableView List<String> getInstantBreakBlockTypeNames() {
        return Collections.unmodifiableList(this.instantBreakBlockTypeNames);
    }

    public void setInstantBreakBlockTypeNames(List<String> instantBreakBlockTypeNames) {
        this.instantBreakBlockTypeNames = new ArrayList<>(instantBreakBlockTypeNames);
    }

    public @NotNull @UnmodifiableView Set<Material> getInstantBreakBlockTypes() {
        if (this.instantBreakBlockTypes != null) {
            return Collections.unmodifiableSet(this.instantBreakBlockTypes);
        }

        List<String> nameList = getInstantBreakBlockTypeNames();
        Set<Material> materialSet = EnumSet.noneOf(Material.class);
        for (String materialName : nameList) {
            Material material = Material.matchMaterial(materialName, false);
            if (material == null) {
                throw new InvalidConfigurationException("Unknown material type '" + materialName + "'.");
            }

            materialSet.add(material);
        }

        this.instantBreakBlockTypes = materialSet;
        return Collections.unmodifiableSet(this.instantBreakBlockTypes);
    }

    public boolean isInstantBreak(@NotNull Material material) {
        Set<Material> types = getInstantBreakBlockTypes();
        return types.contains(material);
    }
}
