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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
        if (!isSonicItem(item)) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);
        Player player = event.getPlayer();
        if (!hasPermission(player)) {
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

    private boolean isSonicItem(@Nullable ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        NamespacedKey itemKey = getItemKey();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (!container.has(itemKey)) {
            return false;
        }

        Boolean value = container.get(itemKey, PersistentDataType.BOOLEAN);
        return (value != null && value);
    }

    private void playActionBar(@NotNull Player player) {
        Component text = Component.text("Whirr!", NamedTextColor.WHITE);
        player.sendActionBar(text);
    }

    private void playSound(@NotNull Player player) {
        World world = player.getWorld();
        Sound sound = getConfiguration().getSound();
        world.playSound(sound, player);
    }

    private boolean hasPermission(@NotNull Permissible permissible) {
        SonicConfiguration configuration = getConfiguration();
        Permission permission = configuration.getPermission();
        return permissible.hasPermission(permission);
    }

    private boolean checkOpenable(@NotNull Block block, @NotNull Block belowBlock) {
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
        Material blockType = block.getType();
        Material aboveType = aboveBlock.getType();
        if (blockType == Material.OBSIDIAN && aboveType.isAir()) {
            aboveBlock.setType(Material.FIRE, true);
            return true;
        }

        return false;
    }

    private boolean checkTNT(@NotNull Block block) {
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

        TNTPrimed tntEntity = world.spawn(location, TNTPrimed.class, this::setupTNT);
        List<Entity> nearbyEntityList = tntEntity.getNearbyEntities(20.0D, 20.0D, 20.0D);
        nearbyEntityList.forEach(this::sendExplosionMessage);
    }

    private void setupTNT(@NotNull TNTPrimed entity) {
        entity.setIsIncendiary(true);
        entity.setFuseTicks(500);
        entity.setYield(100.0F);
    }

    private void sendExplosionMessage(@NotNull Audience audience) {
        Component text = Component.text("As The Doctor would say... RUN!!!!", NamedTextColor.RED, TextDecoration.BOLD);
        audience.sendMessage(text);
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
}
