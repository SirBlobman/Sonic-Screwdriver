package com.github.sirblobman.sonic.screwdriver.listener;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.item.ItemBuilder;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.nms.PlayerHandler;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.sonic.screwdriver.SonicScrewdriverPlugin;

public final class ListenerSonicScrewdriver implements Listener {
    private final SonicScrewdriverPlugin plugin;
    public ListenerSonicScrewdriver(SonicScrewdriverPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if(block == null) return;

        ItemStack item = e.getItem();
        if(!this.plugin.isSonicScrewdriver(item)) return;

        Player player = e.getPlayer();
        if(!hasPermission(player)) return;

        e.setCancelled(true);
        playAction(player);

        Block aboveBlock = block.getRelative(BlockFace.UP);
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        BlockData blockData = block.getBlockData();

        if(blockData instanceof Openable) {
            if(blockData instanceof Door) {
                Door door = (Door) blockData;
                Half half = door.getHalf();
                if(half == Half.TOP) {
                    Door doorBottom = (Door) belowBlock.getBlockData();
                    doorBottom.setOpen(!doorBottom.isOpen());
                    belowBlock.setBlockData(doorBottom);
                    return;
                }
            }

            Openable openable = (Openable) blockData;
            openable.setOpen(!openable.isOpen());
            block.setBlockData(openable);
            return;
        }

        Material blockType = block.getType();
        if(blockType == Material.TNT) {
            Location location = block.getLocation();
            block.setType(Material.AIR, true);
            spawnOverpoweredTNT(location);
            return;
        }

        Material aboveType = aboveBlock.getType();
        if(blockType == Material.OBSIDIAN && aboveType == Material.AIR) {
            aboveBlock.setType(Material.FIRE, true);
            return;
        }

        if(canInstantlyBreak(blockType)) {
            ItemStack drop = new ItemBuilder(blockType).withAmount(1).build();
            World world = block.getWorld();
            Location location = block.getLocation();

            block.setType(Material.AIR, true);
            world.dropItemNaturally(location, drop);
        }
    }

    private boolean hasPermission(Player player) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        String permissionName = configuration.getString("options.permission");
        if(permissionName == null || permissionName.isEmpty()) return true;

        Permission permission = new Permission(permissionName, "Use Sonic Screwdriver Item", PermissionDefault.FALSE);
        return player.hasPermission(permission);
    }

    private void playAction(Player player) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");

        String soundName = configuration.getString("options.sound");
        if(soundName != null && !soundName.isEmpty()) {
            Location location = player.getLocation();
            player.playSound(location, soundName, 1.0F, 1.0F);
        }

        String actionMessage = configuration.getString("language.action-bar");
        if(actionMessage != null && !actionMessage.isEmpty()) {
            String colorMessage = MessageUtility.color(actionMessage);
            MultiVersionHandler multiVersionHandler = this.plugin.getMultiVersionHandler();
            PlayerHandler playerHandler = multiVersionHandler.getPlayerHandler();
            playerHandler.sendActionBar(player, colorMessage);
        }
    }

    private void spawnOverpoweredTNT(Location location) {
        World world = location.getWorld();
        if(world == null) return;

        TNTPrimed tnt = world.spawn(location, TNTPrimed.class, preTNT -> {
            preTNT.setIsIncendiary(true);
            preTNT.setFuseTicks(500);
            preTNT.setYield(100.0F);
        });

        List<Entity> nearbyEntityList = tnt.getNearbyEntities(20.0D, 20.0D, 20.0D);
        nearbyEntityList.forEach(entity -> entity.sendMessage(ChatColor.RED + "&cAs The Doctor would say... RUN!!!!"));
    }

    private Set<Material> getGlassMaterials() {
        Set<Material> glassSet = EnumSet.allOf(Material.class);
        glassSet.removeIf(material -> !material.name().contains("GLASS"));
        return glassSet;
    }

    private boolean canInstantlyBreak(Material material) {
        Set<Material> instantBreakSet = EnumSet.of(Material.COBWEB, Material.LADDER, Material.VINE);
        instantBreakSet.addAll(getGlassMaterials());
        return instantBreakSet.contains(material);
    }
}
