package com.github.sirblobman.sonic.screwdriver.listener;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.item.ItemBuilder;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.listener.PluginListener;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XTag;
import com.github.sirblobman.sonic.screwdriver.SonicScrewdriverPlugin;

public final class ListenerSonicScrewdriver extends PluginListener<SonicScrewdriverPlugin> {
    private final Set<XMaterial> instantBreakSet;

    public ListenerSonicScrewdriver(SonicScrewdriverPlugin plugin) {
        super(plugin);
        this.instantBreakSet = EnumSet.of(XMaterial.COBWEB, XMaterial.LADDER, XMaterial.VINE);
        this.instantBreakSet.addAll(XTag.GLASS.getValues());
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = e.getClickedBlock();
        if(block == null) {
            return;
        }

        ItemStack item = e.getItem();
        SonicScrewdriverPlugin plugin = getPlugin();
        if(!plugin.isSonicScrewdriver(item)) {
            return;
        }

        Player player = e.getPlayer();
        if(!hasPermission(player)) {
            return;
        }

        e.setCancelled(true);
        playAction(player);

        Block aboveBlock = block.getRelative(BlockFace.UP);
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        BlockData blockData = block.getBlockData();

        if(blockData instanceof Openable openable) {
            if(openable instanceof Door door) {
                Half half = door.getHalf();
                if(half == Half.TOP) {
                    Door doorBottom = (Door) belowBlock.getBlockData();
                    doorBottom.setOpen(!doorBottom.isOpen());
                    belowBlock.setBlockData(doorBottom);
                    return;
                }
            }

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
        ConfigurationManager configurationManager = getPlugin().getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        String permissionName = configuration.getString("options.permission");
        if(permissionName == null || permissionName.isEmpty()) {
            return true;
        }

        Permission permission = new Permission(permissionName, "Use Sonic Screwdriver Item", PermissionDefault.FALSE);
        return player.hasPermission(permission);
    }

    private void playAction(Player player) {
        SonicScrewdriverPlugin plugin = getPlugin();
        LanguageManager languageManager = plugin.getLanguageManager();
        languageManager.sendActionBar(player, "action-bar", null);

        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        String soundName = configuration.getString("options.sound");
        if(soundName != null && !soundName.isEmpty()) {
            Location location = player.getLocation();
            player.playSound(location, soundName, 1.0F, 1.0F);
        }
    }

    private boolean canInstantlyBreak(Material blockType) {
        XMaterial material = XMaterial.matchXMaterial(blockType);
        return this.instantBreakSet.contains(material);
    }

    private void spawnOverpoweredTNT(Location location) {
        World world = location.getWorld();
        if(world == null) {
            return;
        }

        TNTPrimed tntEntity = world.spawn(location, TNTPrimed.class, preTntEntity -> {
            preTntEntity.setIsIncendiary(true);
            preTntEntity.setFuseTicks(500);
            preTntEntity.setYield(100.0F);
        });

        List<Entity> nearbyEntityList = tntEntity.getNearbyEntities(20.0D, 20.0D, 20.0D);
        for (Entity entity : nearbyEntityList) {
            sendOverpoweredTntMessage(entity);
        }
    }

    private void sendOverpoweredTntMessage(Entity entity) {
        SonicScrewdriverPlugin plugin = getPlugin();
        LanguageManager languageManager = plugin.getLanguageManager();
        languageManager.sendMessage(entity, "overpowered-tnt-nearby", null);
    }
}
