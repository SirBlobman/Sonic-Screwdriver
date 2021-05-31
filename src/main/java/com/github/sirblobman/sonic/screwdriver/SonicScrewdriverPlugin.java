package com.github.sirblobman.sonic.screwdriver;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.core.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.item.ItemBuilder;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.nms.ItemHandler;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.update.UpdateManager;
import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.sonic.screwdriver.command.CommandSonicScrewdriver;
import com.github.sirblobman.sonic.screwdriver.listener.ListenerSonicScrewdriver;

public final class SonicScrewdriverPlugin extends ConfigurablePlugin {
    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
    }

    @Override
    public void onEnable() {
        new CommandSonicScrewdriver(this).register();

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ListenerSonicScrewdriver(this), this);

        CorePlugin corePlugin = JavaPlugin.getPlugin(CorePlugin.class);
        UpdateManager updateManager = corePlugin.getUpdateManager();
        updateManager.addResource(this, 32859L);
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    public void sendMessage(CommandSender sender, String path) {
        String message = getMessage(path);
        if(message == null || message.isEmpty()) return;
        sender.sendMessage(message);
    }

    public void sendMessage(CommandSender sender, String path, Replacer replacer) {
        String message = getMessage(path);
        if(message == null || message.isEmpty()) return;
        if(replacer != null) message = replacer.replace(message);
        sender.sendMessage(message);
    }

    public ItemStack getSonicScrewdriver() {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        ConfigurationSection section = configuration.getConfigurationSection("options.item");
        if(section == null) throw new IllegalStateException("Invalid Sonic Screwdriver Item In Config!");

        String materialName = section.getString("material");
        if(materialName == null) materialName = "BLAZE_ROD";
        Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
        XMaterial material = optionalMaterial.orElse(XMaterial.BLAZE_ROD);
        ItemBuilder builder = new ItemBuilder(material);

        if(section.isInt("quantity")) {
            int quantity = section.getInt("quantity", 1);
            builder.withAmount(quantity);
        }

        if(section.isInt("damage")) {
            int damage = section.getInt("damage");
            builder.withDamage(damage);
        }

        if(section.isInt("model")) {
            int model = section.getInt("model");
            builder.withModel(model);
        }

        String displayName = section.getString("display-name");
        if(displayName != null && !displayName.isEmpty()) {
            String displayNameColored = MessageUtility.color(displayName);
            builder.withName(displayNameColored);
        }

        List<String> lore = section.getStringList("lore");
        if(!lore.isEmpty()) {
            List<String> loreColored = MessageUtility.colorList(lore);
            builder.withLore(loreColored);
        }

        ItemStack item = builder.build();
        MultiVersionHandler multiVersionHandler = getMultiVersionHandler();
        ItemHandler itemHandler = multiVersionHandler.getItemHandler();
        return itemHandler.setCustomNBT(item, "sonic_screwdriver", "yes");
    }

    public boolean isSonicScrewdriver(ItemStack item) {
        if(ItemUtility.isAir(item)) return false;
        MultiVersionHandler multiVersionHandler = getMultiVersionHandler();
        ItemHandler itemHandler = multiVersionHandler.getItemHandler();

        String customNBT = itemHandler.getCustomNBT(item, "sonic_screwdriver", "no");
        return (customNBT != null && customNBT.equals("yes"));
    }

    private String getMessage(String path) {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if(configuration.isList(path)) {
            List<String> messageList = configuration.getStringList(path);
            if(!messageList.isEmpty()) {
                List<String> coloredList = MessageUtility.colorList(messageList);
                return String.join("\n", coloredList);
            }
        }

        if(configuration.isString(path)) {
            String message = configuration.getString(path);
            if(message != null) return MessageUtility.color(message);
        }

        return String.format("{%s}", path);
    }
}
