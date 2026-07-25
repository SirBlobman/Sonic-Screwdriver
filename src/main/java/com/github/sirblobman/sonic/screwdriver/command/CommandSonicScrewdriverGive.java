package com.github.sirblobman.sonic.screwdriver.command;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
import com.github.sirblobman.sonic.screwdriver.configuration.SonicConfiguration;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
        ItemConfiguration sonicItem = configuration.getSonicItem();

        ItemStack item = sonicItem.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(itemKey, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(itemMeta);

        PlayerGiveResult giveResult = target.give(Set.of(item), false);
        if (!giveResult.leftovers().isEmpty()) {
            Component text = Component.text("Failed to give sonic item to player '", NamedTextColor.RED)
                    .append(asComponent(target)).append(Component.text("'."));
            sender.sendMessage(text);
        } else {
            Component text = Component.text("Successfully gave sonic item to player '", NamedTextColor.GREEN)
                    .append(asComponent(target)).append(Component.text("'."));
            sender.sendMessage(text);
        }

        return Command.SINGLE_SUCCESS;
    }

    private @NotNull Component asComponent(@NotNull Entity entity) {
        Component name = entity.name();
        return name.hoverEvent(entity.asHoverEvent());
    }
}
