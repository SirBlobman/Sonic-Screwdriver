package com.github.sirblobman.sonic.screwdriver.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.sonic.screwdriver.SonicScrewdriverPlugin;

public final class CommandSonicScrewdriver extends Command {
    private final SonicScrewdriverPlugin plugin;
    public CommandSonicScrewdriver(SonicScrewdriverPlugin plugin) {
        super(plugin, "sonic-screwdriver");
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1) {
            Set<String> valueSet = getOnlinePlayerNames();
            return getMatching(valueSet, args[0]);
        }

        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length == 0 && !(sender instanceof Player)) {
            this.plugin.sendMessage(sender, "language.not-player");
            return true;
        }

        String targetName = (args.length < 1 ? sender.getName() : args[0]);
        Player target = Bukkit.getPlayer(targetName);
        if(target == null) {
            this.plugin.sendMessage(sender, "language.invalid-target");
            return false;
        }
        String realTargetName = target.getName();

        ItemStack item = this.plugin.getSonicScrewdriver();
        giveItems(target, item);

        this.plugin.sendMessage(sender, "language.successful-give", message -> message.replace("{target}", realTargetName));
        this.plugin.sendMessage(target, "language.give-sonic");
        return true;
    }
}