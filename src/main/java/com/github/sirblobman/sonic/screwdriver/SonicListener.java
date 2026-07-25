package com.github.sirblobman.sonic.screwdriver;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.sonic.screwdriver.configuration.MessageConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.PermissionConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.SoundConfiguration;

import net.kyori.adventure.audience.Audience;

public final class SonicListener implements Listener {
    private final SonicPlugin plugin;

    public SonicListener(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    private @NotNull NamespacedKey getItemKey() {
        return getPlugin().getItemKey();
    }

    private @NotNull SonicConfiguration getConfiguration() {
        return getPlugin().getConfiguration();
    }

    public void register() {
        SonicPlugin plugin = getPlugin();
        Server server = plugin.getServer();
        PluginManager pluginManager = server.getPluginManager();
        pluginManager.registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (isNotSonicItem(item)) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);
        Player player = event.getPlayer();
        if (isLackingPermission(player)) {
            return;
        }

        Block belowBlock = block.getRelative(BlockFace.DOWN);
        if (checkOpenable(block, belowBlock)) {
            playActionBar(player);
            playSound(player);
            return;
        }

        Block aboveBlock = block.getRelative(BlockFace.UP);
        if (checkPortal(block, aboveBlock)) {
            playActionBar(player);
            playSound(player);
            return;
        }

        if (checkTNT(block)) {
            playActionBar(player);
            playSound(player);
            return;
        }

        if (checkInstantlyBreak(block)) {
            playActionBar(player);
            playSound(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractAtEntityEvent e) {
        Player player = e.getPlayer();
        Entity entity = e.getRightClicked();
        EquipmentSlot hand = e.getHand();
        ItemStack heldItem = player.getInventory().getItem(hand);
        if (isNotSonicItem(heldItem)) {
            return;
        }

        e.setCancelled(true);
        if (isLackingPermission(player)) {
            return;
        }

        if (checkEnhanceCreeper(player, entity)) {
            playActionBar(player);
            playSound(player);
        }
    }

    private boolean isNotSonicItem(@Nullable ItemStack item) {
        if (item == null) {
            return true;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return true;
        }

        NamespacedKey itemKey = getItemKey();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(itemKey)) {
            return true;
        }

        Boolean value = container.get(itemKey, PersistentDataType.BOOLEAN);
        return (value == null || !value);
    }

    private void playActionBar(@NotNull Player player) {
        MessageConfiguration messageConfiguration = getPlugin().getMessageConfiguration();
        messageConfiguration.sendActionBar(player, "action-bar");
    }

    private void playSound(@NotNull Player player) {
        SonicConfiguration configuration = getConfiguration();
        SoundConfiguration soundConfiguration = configuration.getSound();
        soundConfiguration.play(player.getWorld(), player);
    }

    private boolean isLackingPermission(@NotNull Permissible permissible) {
        SonicConfiguration configuration = getConfiguration();
        PermissionConfiguration permissionConfiguration = configuration.getPermission();
        return !permissionConfiguration.hasPermission(permissible);
    }

    private boolean checkOpenable(@NotNull Block block, @NotNull Block belowBlock) {
        SonicConfiguration configuration = getConfiguration();
        if (!configuration.hasAbilityOpenDoors()) {
            return false;
        }

        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Openable openable)) {
            return false;
        }

        if (openable instanceof Door door) {
            Bisected.Half half = door.getHalf();
            if (half == Bisected.Half.TOP) {
                Door doorBottom = (Door) belowBlock.getBlockData();
                toggleOpenable(belowBlock, doorBottom);
                return true;
            }
        }

        toggleOpenable(block, openable);
        return true;
    }

    private void toggleOpenable(@NotNull Block block, @NotNull Openable openable) {
        boolean opposite = !openable.isOpen();
        openable.setOpen(opposite);
        block.setBlockData(openable);
    }

    private boolean checkPortal(@NotNull Block block, @NotNull Block aboveBlock) {
        SonicConfiguration configuration = getConfiguration();
        if (!configuration.hasAbilityLightNetherPortal()) {
            return false;
        }

        Material blockType = block.getType();
        Material aboveType = aboveBlock.getType();
        if (blockType == Material.OBSIDIAN && aboveType.isAir()) {
            aboveBlock.setType(Material.FIRE, true);
            return true;
        }

        return false;
    }

    private boolean checkTNT(@NotNull Block block) {
        SonicConfiguration configuration = getConfiguration();
        if (!configuration.hasAbilityCreateOverpoweredTNT()) {
            return false;
        }

        Material blockType = block.getType();
        if (blockType != Material.TNT) {
            return false;
        }

        Location location = block.getLocation();
        block.setType(Material.AIR, true);
        spawnOverpoweredTNT(location);
        return true;
    }

    private void spawnOverpoweredTNT(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        double messageRadius = getConfiguration().getOverpoweredTntMessageRadius();
        TNTPrimed tntEntity = world.spawn(location, TNTPrimed.class, this::setupTNT);
        List<Entity> nearbyEntityList = tntEntity.getNearbyEntities(messageRadius, messageRadius, messageRadius);
        nearbyEntityList.forEach(this::sendExplosionMessage);
    }

    private void setupTNT(@NotNull TNTPrimed entity) {
        SonicConfiguration configuration = getConfiguration();
        boolean incendiary = configuration.isOverpoweredTntIncendiary();
        float explosionYield = configuration.getOverpoweredTntExplosionYield();
        int fuseTicks = configuration.getOverpoweredTntFuseTicks();

        entity.setIsIncendiary(incendiary);
        entity.setYield(explosionYield);
        entity.setFuseTicks(fuseTicks);
    }

    private void sendExplosionMessage(@NotNull Audience audience) {
        MessageConfiguration messageConfiguration = getPlugin().getMessageConfiguration();
        messageConfiguration.sendMessage(audience, "overpowered-tnt-nearby");
    }

    private boolean checkInstantlyBreak(@NotNull Block block) {
        SonicConfiguration configuration = getConfiguration();
        if (!configuration.hasAbilityInstantBreakBlocks()) {
            return false;
        }

        Material blockType = block.getType();
        if (!configuration.isInstantBreak(blockType)) {
            return false;
        }

        ItemStack item = new ItemStack(blockType, 1);
        World world = block.getWorld();
        Location location = block.getLocation();

        block.setType(Material.AIR, true);
        world.dropItemNaturally(location, item);
        return true;
    }

    private boolean checkEnhanceCreeper(@NotNull Player player, @NotNull Entity entity) {
        SonicConfiguration configuration = getConfiguration();
        if (!configuration.hasAbilityEnhanceCreepers()) {
            return false;
        }

        if (!(entity instanceof Creeper creeper)) {
            return false;
        }

        if (isEnhancedCreeper(creeper)) {
            creeper.ignite(player);
            return true;
        }

        enhanceCreeper(creeper);
        return true;
    }

    private boolean isEnhancedCreeper(@NotNull Creeper creeper) {
        NamespacedKey enhanced = getPlugin().getEnhancedKey();
        PersistentDataContainer container = creeper.getPersistentDataContainer();
        if (container.has(enhanced)) {
            Boolean value = container.get(enhanced, PersistentDataType.BOOLEAN);
            return (value != null && value);
        }

        return false;
    }

    private void enhanceCreeper(@NotNull Creeper creeper) {
        boolean powered = getConfiguration().isEnhancedCreeperPowered();
        int explosionRadius = getConfiguration().getEnhancedCreeperExplosionRadius();
        NamespacedKey enhanced = getPlugin().getEnhancedKey();

        PersistentDataContainer container = creeper.getPersistentDataContainer();
        container.set(enhanced, PersistentDataType.BOOLEAN, true);
        creeper.setExplosionRadius(explosionRadius);
        creeper.setPowered(powered);
    }
}
