package com.github.sirblobman.sonic.screwdriver.command;

import org.jetbrains.annotations.NotNull;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

import com.github.sirblobman.sonic.screwdriver.SonicPlugin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

public final class CommandSonicScrewdriver {
    private final SonicPlugin plugin;

    public CommandSonicScrewdriver(@NotNull SonicPlugin plugin) {
        this.plugin = plugin;
    }

    private @NotNull SonicPlugin getPlugin() {
        return this.plugin;
    }

    public void register(@NotNull Commands registrar) {
        registrar.register(setupCommand());
    }

    private @NotNull LiteralCommandNode<CommandSourceStack> setupCommand() {
        SonicPlugin plugin = getPlugin();
        LiteralArgumentBuilder<CommandSourceStack> reloadCommand = Commands.literal("reload");
        reloadCommand.requires(new PermissionRequirement("sonic.screwdriver.command.reload"));
        reloadCommand.executes(new CommandSonicScrewdriverReload(plugin));

        LiteralArgumentBuilder<CommandSourceStack> giveCommand = Commands.literal("give");
        giveCommand.requires(new PermissionRequirement("sonic.screwdriver.command.reload.give"));
        giveCommand.then(Commands.argument("player", ArgumentTypes.player())
                .executes(new CommandSonicScrewdriverGive(plugin)));

        LiteralArgumentBuilder<CommandSourceStack> commandRoot = Commands.literal("sonic-screwdriver");
        commandRoot.requires(new PermissionRequirement("sonic.screwdriver.command"));
        commandRoot.then(reloadCommand);
        commandRoot.then(giveCommand);
        return commandRoot.build();
    }
}
