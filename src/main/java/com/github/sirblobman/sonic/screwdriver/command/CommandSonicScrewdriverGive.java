package com.github.sirblobman.sonic.screwdriver.command;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.entity.PlayerGiveResult;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;
import com.github.sirblobman.sonic.screwdriver.configuration.ItemConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.MessageConfiguration;
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;
import com.github.sirblobman.sonic.screwdriver.message.EntityReplacer;
import com.github.sirblobman.sonic.screwdriver.message.Replacer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CommandSonicScrewdriverGive implements Command<CommandSourceStack> {
    private final SonicPlugin plugin;

    public CommandSonicScrewdriverGive(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        CommandSender sender = source.getSender();

        PlayerSelectorArgumentResolver selector = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player target = selector.resolve(source).getFirst();

        SonicPlugin plugin = getPlugin();
        NamespacedKey itemKey = plugin.getItemKey();
        SonicConfiguration configuration = plugin.getConfiguration();
        ItemConfiguration sonicItem = configuration.getItem();

        ItemStack item = sonicItem.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(itemKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(itemMeta);

        Replacer replacer = new EntityReplacer("{target}", target);
        MessageConfiguration messageConfiguration = plugin.getMessageConfiguration();
        PlayerGiveResult giveResult = target.give(Set.of(item), false);
        if (!giveResult.leftovers().isEmpty()) {
            messageConfiguration.sendMessage(sender, "give-sonic-fail", replacer);
        } else {
            messageConfiguration.sendMessage(sender, "give-sonic-sender", replacer);
            messageConfiguration.sendMessage(target, "give-sonic-target", replacer);
        }

        return Command.SINGLE_SUCCESS;
    }
}
