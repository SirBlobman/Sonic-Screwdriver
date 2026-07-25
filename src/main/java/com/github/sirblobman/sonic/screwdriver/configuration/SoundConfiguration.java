package com.github.sirblobman.sonic.screwdriver.configuration;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;

import static com.github.sirblobman.sonic.screwdriver.configuration.ConfigurationHelper.getFloat;

public final class SoundConfiguration {
    private boolean enabled;
    private @NotNull String soundKeyString;
    private @NotNull String soundCategoryName;
    private float volume;
    private float pitch;

    private transient Sound sound;

    public SoundConfiguration() {
        this.enabled = true;
        this.soundKeyString = "minecraft:ambient.cave";
        this.soundCategoryName = "master";
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.sound = null;
    }

    public void load(@NotNull ConfigurationSection config) {
        setEnabled(config.getBoolean("enabled", true));
        setSoundKeyString(config.getString("sound", "minecraft:ambient.cave"));
        setSoundCategoryName(config.getString("category", "master"));
        setVolume(getFloat(config, "volume", 1.0F));
        setPitch(getFloat(config, "pitch", 1.0F));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public @NotNull String getSoundKeyString() {
        return this.soundKeyString;
    }

    public void setSoundKeyString(@NotNull String soundKeyString) {
        this.soundKeyString = soundKeyString;
        this.sound = null;
    }

    public @NotNull String getSoundCategoryName() {
        return this.soundCategoryName;
    }

    public void setSoundCategoryName(@NotNull String soundCategoryName) {
        this.soundCategoryName = soundCategoryName;
        this.sound = null;
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public @NotNull Sound getSound() {
        if (this.sound != null) {
            return this.sound;
        }

        String soundKeyString = getSoundKeyString();
        NamespacedKey soundKey = NamespacedKey.fromString(soundKeyString);
        if (soundKey == null) {
            throw new InvalidConfigurationException("Failed to parse sound key from string '" + soundKeyString + "'.");
        }

        String soundCategoryName = getSoundCategoryName();
        Sound.Source soundCategory;

        try {
            soundCategory = Sound.Source.valueOf(soundCategoryName.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ex) {
            throw new InvalidConfigurationException("Unknown sound category '" + soundCategoryName + "'.");
        }

        float volume = getVolume();
        float pitch = getPitch();
        return (this.sound = Sound.sound(soundKey, soundCategory, volume, pitch));
    }

    public void play(@NotNull Audience audience) {
        Sound.Emitter emitter = Sound.Emitter.self();
        play(audience, emitter);
    }

    public void play(@NotNull Audience audience, @NotNull Sound.Emitter emitter) {
        if (isEnabled()) {
            Sound sound = getSound();
            audience.playSound(sound, emitter);
        }
    }
}
