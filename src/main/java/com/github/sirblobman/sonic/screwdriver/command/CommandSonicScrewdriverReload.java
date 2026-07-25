package com.github.sirblobman.sonic.screwdriver.command;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.CommandSender;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;
import com.github.sirblobman.sonic.screwdriver.configuration.InvalidConfigurationException;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSonicScrewdriverReload implements Command<CommandSourceStack> {
    private final SonicPlugin plugin;

    public CommandSonicScrewdriverReload(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            SonicPlugin plugin = getPlugin();
            plugin.reloadConfiguration();

            CommandSender sender = context.getSource().getSender();
            Component message = Component.text("Successfully reloaded the Sonic Screwdriver configuration files.", NamedTextColor.GREEN);
            sender.sendMessage(message);
        } catch (InvalidConfigurationException ex) {
            CommandSender sender = context.getSource().getSender();
            Component message = Component.text("Failed to reload configuration files. Check the server logs for error messages.", NamedTextColor.RED);
            sender.sendMessage(message);
        }

        return Command.SINGLE_SUCCESS;
    }
}
