package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;

import static com.github.sirblobman.sonic.screwdriver.configuration.ConfigurationHelper.getFloat;

public final class SonicConfiguration {
    private final SonicPlugin plugin;
    private final ItemConfiguration item;
    private final PermissionConfiguration permission;
    private final SoundConfiguration sound;

    private boolean debugMode;

    private boolean abilityOpenDoors;
    private boolean abilityLightNetherPortal;
    private boolean abilityInstantBreakBlocks;
    private boolean abilityCreateOverpoweredTNT;
    private boolean abilityEnhanceCreepers;

    private double overpoweredTntMessageRadius;
    private float overpoweredTntExplosionYield;
    private boolean overpoweredTntIncendiary;
    private int overpoweredTntFuseTicks;

    private boolean enhancedCreeperPowered;
    private int enhancedCreeperExplosionRadius;

    private List<String> instantBreakBlockTypeNames;

    private transient Set<Material> instantBreakBlockTypes;

    public SonicConfiguration(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
        this.item = new ItemConfiguration(plugin);
        this.permission = new PermissionConfiguration();
        this.sound = new SoundConfiguration();
    }

    public void load(@NotNull ConfigurationSection config) {
        ConfigurationSection item = config.getConfigurationSection("item");
        if (item == null) {
            throw new InvalidConfigurationException("Missing section 'item:' in config.yml");
        }

        ConfigurationSection permission = config.getConfigurationSection("permission");
        if (permission == null) {
            throw new InvalidConfigurationException("Missing section 'permission:' in config.yml");
        }

        ConfigurationSection sound = config.getConfigurationSection("sound");
        if (sound == null) {
            throw new InvalidConfigurationException("Missing section 'sound:' in config.yml");
        }

        ItemConfiguration itemConfiguration = getItem();
        PermissionConfiguration permissionConfiguration = getPermission();
        SoundConfiguration soundConfiguration = getSound();
        itemConfiguration.load(item);
        permissionConfiguration.load(permission);
        soundConfiguration.load(sound);

        setAbilityOpenDoors(config.getBoolean("abilities.open-doors", true));
        setAbilityLightNetherPortal(config.getBoolean("abilities.light-nether-portal", true));
        setAbilityInstantBreakBlocks(config.getBoolean("abilities.instant-break-blocks", true));
        setAbilityCreateOverpoweredTNT(config.getBoolean("abilities.create-overpowered-tnt", true));
        setAbilityEnhanceCreepers(config.getBoolean("abilities.enhance-creepers", true));

        setOverpoweredTntExplosionYield(getFloat(config, "overpowered-tnt.explosion-yield", 100.0F));
        setOverpoweredTntFuseTicks(config.getInt("overpowered-tnt.fuse-ticks", 500));
        setOverpoweredTntIncendiary(config.getBoolean("overpowered-tnt.incendiary", true));
        setOverpoweredTntMessageRadius(config.getDouble("overpowered-tnt.message-radius", 20.0D));

        setEnhancedCreeperExplosionRadius(config.getInt("enhanced-creeper.explosion-radius", 100));
        setEnhancedCreeperPowered(config.getBoolean("enhanced-creeper.powered", true));

        setDebugMode(config.getBoolean("debug-mode", false));
        setInstantBreakBlockTypeNames(config.getStringList("instant-break-block-types"));
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

    public @NotNull ItemConfiguration getItem() {
        return this.item;
    }

    public @NotNull PermissionConfiguration getPermission() {
        return this.permission;
    }

    public @NotNull SoundConfiguration getSound() {
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

    public boolean hasAbilityEnhanceCreepers() {
        return this.abilityEnhanceCreepers;
    }

    public void setAbilityEnhanceCreepers(boolean abilityEnhanceCreepers) {
        this.abilityEnhanceCreepers = abilityEnhanceCreepers;
    }

    public double getOverpoweredTntMessageRadius() {
        return this.overpoweredTntMessageRadius;
    }

    public void setOverpoweredTntMessageRadius(double overpoweredTntMessageRadius) {
        this.overpoweredTntMessageRadius = overpoweredTntMessageRadius;
    }

    public float getOverpoweredTntExplosionYield() {
        return this.overpoweredTntExplosionYield;
    }

    public void setOverpoweredTntExplosionYield(float overpoweredTntExplosionYield) {
        this.overpoweredTntExplosionYield = overpoweredTntExplosionYield;
    }

    public boolean isOverpoweredTntIncendiary() {
        return this.overpoweredTntIncendiary;
    }

    public void setOverpoweredTntIncendiary(boolean overpoweredTntIncendiary) {
        this.overpoweredTntIncendiary = overpoweredTntIncendiary;
    }

    public int getOverpoweredTntFuseTicks() {
        return this.overpoweredTntFuseTicks;
    }

    public void setOverpoweredTntFuseTicks(int overpoweredTntFuseTicks) {
        this.overpoweredTntFuseTicks = overpoweredTntFuseTicks;
    }

    public boolean isEnhancedCreeperPowered() {
        return this.enhancedCreeperPowered;
    }

    public void setEnhancedCreeperPowered(boolean enhancedCreeperPowered) {
        this.enhancedCreeperPowered = enhancedCreeperPowered;
    }

    public int getEnhancedCreeperExplosionRadius() {
        return this.enhancedCreeperExplosionRadius;
    }

    public void setEnhancedCreeperExplosionRadius(int enhancedCreeperExplosionRadius) {
        this.enhancedCreeperExplosionRadius = enhancedCreeperExplosionRadius;
    }
}
