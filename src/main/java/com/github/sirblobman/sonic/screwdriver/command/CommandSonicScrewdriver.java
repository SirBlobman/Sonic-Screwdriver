package com.github.sirblobman.sonic.screwdriver.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.language.SimpleReplacer;
import com.github.sirblobman.sonic.screwdriver.SonicScrewdriverPlugin;

public final class CommandSonicScrewdriver extends Command {
    private final SonicScrewdriverPlugin plugin;

    public CommandSonicScrewdriver(SonicScrewdriverPlugin plugin) {
        super(plugin, "sonic-screwdriver");
        setPermissionName("sonic.screwdriver.give");
        this.plugin = plugin;
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
        if(args.length == 0 && !(sender instanceof Player)) {
            sendMessage(sender, "error.player-only", null);
            return true;
        }

        String targetName = (args.length < 1 ? sender.getName() : args[0]);
        Player target = findTarget(sender, targetName);
        if(target == null) {
            return true;
        }

        String realTargetName = target.getName();
        SonicScrewdriverPlugin plugin = getSonicScrewdriverPlugin();
        ItemStack item = plugin.getSonicScrewdriver();
        giveItems(target, item);

        Replacer replacer = new SimpleReplacer("{target}", realTargetName);
        sendMessage(sender, "successful-give", replacer);
        sendMessage(target, "give-sonic", null);
        return true;
    }

    private SonicScrewdriverPlugin getSonicScrewdriverPlugin() {
        return this.plugin;
    }
}
