package com.github.sirblobman.sonic.screwdriver.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.sonic.screwdriver.SonicScrewdriverPlugin;

import org.jetbrains.annotations.NotNull;

public final class CommandSonicScrewdriver extends Command {
    private final SonicScrewdriverPlugin plugin;
    public CommandSonicScrewdriver(SonicScrewdriverPlugin plugin) {
        super(plugin, "sonic-screwdriver");
        this.plugin = plugin;
    }

    @NotNull
    @Override
    protected LanguageManager getLanguageManager() {
        return this.plugin.getLanguageManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            Set<String> valueSet = getOnlinePlayerNames();
            return getMatching(args[0], valueSet);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LanguageManager languageManager = getLanguageManager();
        if(args.length == 0 && !(sender instanceof Player)) {
            languageManager.sendMessage(sender, "error.player-only", null, true);
            return true;
        }

        String targetName = (args.length < 1 ? sender.getName() : args[0]);
        Player target = findTarget(sender, targetName);
        if(target == null) {
            return true;
        }

        String realTargetName = target.getName();
        ItemStack item = this.plugin.getSonicScrewdriver();
        giveItems(target, item);

        Replacer replacer = message -> message.replace("{target}", realTargetName);
        languageManager.sendMessage(sender, "successful-give", replacer, true);
        languageManager.sendMessage(target, "give-sonic", null, true);
        return true;
    }
}
