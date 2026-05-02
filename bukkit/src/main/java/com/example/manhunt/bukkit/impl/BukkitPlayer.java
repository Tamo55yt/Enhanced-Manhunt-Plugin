package com.example.manhunt.bukkit.impl;

import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitPlayer implements MPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull String getName() { return player.getName(); }

    @Override
    public @NotNull UUID getUniqueId() { return player.getUniqueId(); }

    private String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        player.sendMessage(translate(message));
    }

    @Override
    public void sendActionBar(@NotNull String message) {
        try {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                new net.md_5.bungee.api.chat.TextComponent(translate(message)));
        } catch (Exception e) {
            player.sendMessage(translate(message));
        }
    }

    @Override
    public void sendTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(translate(title), translate(subtitle), fadeIn, stay, fadeOut);
    }

    @Override
    public MLocation getLocation() {
        Location loc = player.getLocation();
        return new MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public void teleport(MLocation location) {
        org.bukkit.World world = Bukkit.getWorld(location.getWorldName());
        if (world != null) {
            player.teleport(new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));
        }
    }

    @Override
    public void setCompassTarget(MLocation location) {
        org.bukkit.World world = Bukkit.getWorld(location.getWorldName());
        if (world != null) {
            player.setCompassTarget(new Location(world, location.getX(), location.getY(), location.getZ()));
        }
    }

    @Override
    public void updateInventoryCompass(MLocation target) {
        org.bukkit.World world = Bukkit.getWorld(target.getWorldName());
        if (world == null) return;
        Location loc = new Location(world, target.getX(), target.getY(), target.getZ());

        boolean updated = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.COMPASS) continue;

            try {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                java.lang.reflect.Method setLodestone = null;
                try {
                    setLodestone = meta.getClass().getMethod("setLodestone", Location.class);
                } catch (NoSuchMethodException e) {
                    for (Class<?> itf : meta.getClass().getInterfaces()) {
                        try {
                            setLodestone = itf.getMethod("setLodestone", Location.class);
                            if (setLodestone != null) break;
                        } catch (NoSuchMethodException ignored) {}
                    }
                }

                if (setLodestone != null) {
                    java.lang.reflect.Method setLodestoneTracked = meta.getClass().getMethod("setLodestoneTracked", boolean.class);
                    setLodestone.invoke(meta, loc);
                    setLodestoneTracked.invoke(meta, false);
                    item.setItemMeta(meta);
                    updated = true;
                }
            } catch (Exception ignored) {}
        }

        if (!updated) {
            player.setCompassTarget(loc);
        }
    }

    @Override
    public double getHealth() { return player.getHealth(); }

    @Override
    @SuppressWarnings("deprecation")
    public void setHealth(double health) {
        double max;
        try {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            max = (attr != null) ? attr.getValue() : player.getMaxHealth();
        } catch (Throwable t) {
            max = player.getMaxHealth();
        }
        player.setHealth(Math.min(health, max));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMaxHealth(double health) {
        try {
            org.bukkit.attribute.AttributeInstance attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(health);
            } else {
                player.setMaxHealth(health);
            }
        } catch (Throwable t) {
            player.setMaxHealth(health);
        }
    }

    @Override
    public void clearInventory() { player.getInventory().clear(); }

    @Override
    public void giveItem(@NotNull String material, int amount) {
        try {
            Material mat = Material.valueOf(material.toUpperCase());
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(mat, amount));
        } catch (Exception ignored) {}
    }

    @Override
    public void addPotionEffect(@NotNull String effectType, int duration, int amplifier) {
        try {
            org.bukkit.potion.PotionEffectType type = getPotionType(effectType);
            if (type != null) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier, false, false));
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("deprecation")
    private org.bukkit.potion.PotionEffectType getPotionType(String name) {
        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.minecraft(name.toLowerCase());
            org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByKey(key);
            if (type != null) return type;
        } catch (Throwable ignored) {}
        return org.bukkit.potion.PotionEffectType.getByName(name.toUpperCase());
    }

    @Override
    public void setGameMode(String mode) {
        try {
            player.setGameMode(GameMode.valueOf(mode.toUpperCase()));
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isOnline() { return player.isOnline(); }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MPlayer) return ((MPlayer) obj).getUniqueId().equals(getUniqueId());
        return false;
    }

    public Player getBukkitPlayer() { return player; }
}
