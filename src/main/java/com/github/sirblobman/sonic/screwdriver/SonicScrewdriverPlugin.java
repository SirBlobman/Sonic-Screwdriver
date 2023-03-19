package com.github.sirblobman.sonic.screwdriver;

import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.bstats.bukkit.Metrics;
import com.github.sirblobman.api.bstats.charts.SimplePie;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.item.ItemBuilder;
import com.github.sirblobman.api.language.ComponentHelper;
import com.github.sirblobman.api.language.Language;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.nbt.CustomNbtContainer;
import com.github.sirblobman.api.nbt.CustomNbtTypes;
import com.github.sirblobman.api.nms.ItemHandler;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.update.UpdateManager;
import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.sonic.screwdriver.command.CommandSonicScrewdriver;
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;
import com.github.sirblobman.sonic.screwdriver.listener.ListenerSonicScrewdriver;

public final class SonicScrewdriverPlugin extends ConfigurablePlugin {
    private final SonicConfiguration configuration;

    public SonicScrewdriverPlugin() {
        this.configuration = new SonicConfiguration();
    }

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

        LanguageManager languageManager = getLanguageManager();
        languageManager.onPluginEnable();

        registerCommands();
        registerListeners();
        registerUpdateChecker();
        register_bStats();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    @Override
    protected void reloadConfiguration() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");

        YamlConfiguration yamlConfiguration = configurationManager.get("config.yml");
        SonicConfiguration configuration = getConfiguration();
        configuration.load(yamlConfiguration);

        LanguageManager languageManager = getLanguageManager();
        languageManager.reloadLanguages();
    }

    public SonicConfiguration getConfiguration() {
        return this.configuration;
    }

    public ItemStack getSonicScrewdriver(Player player) {
        SonicConfiguration configuration = getConfiguration();
        XMaterial material = configuration.getItemMaterial();
        ItemBuilder builder = new ItemBuilder(material);

        int quantity = configuration.getItemQuantity();
        builder.withAmount(quantity);

        int damage = configuration.getItemDamage();
        builder.withDamage(damage);

        Integer modelData = configuration.getItemCustomModelData();
        builder.withModel(modelData);

        ItemStack item = builder.build();
        MultiVersionHandler multiVersionHandler = getMultiVersionHandler();
        ItemHandler itemHandler = multiVersionHandler.getItemHandler();

        Component displayName = getSonicScrewdriverDisplayName(player);
        List<Component> lore = getSonicScrewdriverLore(player);
        item = itemHandler.setDisplayName(item, displayName);
        item = itemHandler.setLore(item, lore);

        CustomNbtContainer customNbt = itemHandler.getCustomNbt(item);
        customNbt.set("sonic-screwdriver", CustomNbtTypes.BOOLEAN, true);
        return itemHandler.setCustomNbt(item, customNbt);
    }

    private Component getSonicScrewdriverDisplayName(Player player) {
        LanguageManager languageManager = getLanguageManager();
        Component displayName = languageManager.getMessage(player, "sonic-screwdriver.display-name");
        return ComponentHelper.wrapNoItalics(displayName);
    }

    private List<Component> getSonicScrewdriverLore(Player player) {
        LanguageManager languageManager = getLanguageManager();
        List<Component> loreList = languageManager.getMessageList(player, "sonic-screwdriver.lore");
        return ComponentHelper.wrapNoItalics(loreList);
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

    private void register_bStats() {
        Metrics metrics = new Metrics(this, 16256);
        metrics.addCustomChart(new SimplePie("selected_language", this::getDefaultLanguageCode));
    }

    private String getDefaultLanguageCode() {
        LanguageManager languageManager = getLanguageManager();
        Language defaultLanguage = languageManager.getDefaultLanguage();
        return (defaultLanguage == null ? "none" : defaultLanguage.getLanguageName());
    }
}
