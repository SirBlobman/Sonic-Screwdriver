package com.github.sirblobman.sonic.screwdriver;

import java.util.List;
import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.bstats.bukkit.Metrics;
import com.github.sirblobman.api.bstats.charts.SimplePie;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.item.ItemBuilder;
import com.github.sirblobman.api.language.Language;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.nbt.CustomNbtContainer;
import com.github.sirblobman.api.nbt.CustomNbtTypes;
import com.github.sirblobman.api.nms.ItemHandler;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
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

        LanguageManager languageManager = getLanguageManager();
        languageManager.saveDefaultLanguageFiles();
    }

    @Override
    public void onEnable() {
        reloadConfig();

        registerCommands();
        registerListeners();
        registerUpdateChecker();
        registerbStats();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    @Override
    protected void reloadConfiguration() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.reloadLanguageFiles();
    }

    public ItemStack getSonicScrewdriver() {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        ConfigurationSection section = configuration.getConfigurationSection("options.item");
        if(section == null) {
            throw new IllegalStateException("Invalid Sonic Screwdriver Item In Config!");
        }

        String materialName = section.getString("material");
        if(materialName == null) {
            materialName = "BLAZE_ROD";
        }

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

        CustomNbtContainer customNbt = itemHandler.getCustomNbt(item);
        customNbt.set("sonic-screwdriver", CustomNbtTypes.BOOLEAN, true);
        return itemHandler.setCustomNbt(item, customNbt);
    }

    public boolean isSonicScrewdriver(ItemStack item) {
        if(ItemUtility.isAir(item)) {
            return false;
        }

        MultiVersionHandler multiVersionHandler = getMultiVersionHandler();
        ItemHandler itemHandler = multiVersionHandler.getItemHandler();
        CustomNbtContainer customNbt = itemHandler.getCustomNbt(item);
        return customNbt.getOrDefault("sonic-screwdriver", CustomNbtTypes.BOOLEAN, false);
    }

    private void registerCommands() {
        new CommandSonicScrewdriver(this).register();
    }

    private void registerListeners() {
        new ListenerSonicScrewdriver(this).register();
    }

    private void registerUpdateChecker() {
        CorePlugin corePlugin = JavaPlugin.getPlugin(CorePlugin.class);
        UpdateManager updateManager = corePlugin.getUpdateManager();
        updateManager.addResource(this, 32859L);
    }

    private void registerbStats() {
        Metrics metrics = new Metrics(this, 16256);
        metrics.addCustomChart(new SimplePie("selected_language", this::getDefaultLanguageCode));
    }

    private String getDefaultLanguageCode() {
        LanguageManager languageManager = getLanguageManager();
        Language defaultLanguage = languageManager.getDefaultLanguage();
        return (defaultLanguage == null ? "none" : defaultLanguage.getLanguageCode());
    }
}
