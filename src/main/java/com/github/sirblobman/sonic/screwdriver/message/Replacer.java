package com.github.sirblobman.sonic.screwdriver.message;

import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public abstract class Replacer {
    private final String source;

    public Replacer(@NotNull String source) {
        this.source = source;
    }

    public @NotNull String getSource() {
        return this.source;
    }

    public abstract @NotNull Component getReplacement();
}
