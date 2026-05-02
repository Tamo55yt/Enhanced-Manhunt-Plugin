package com.example.manhunt.bukkit.impl;

import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MScheduler;
import com.example.manhunt.api.MWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitPlatform implements MPlatform {
    private final Plugin plugin;
    private final MScheduler scheduler;

    public BukkitPlatform(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = new BukkitScheduler(plugin);
    }

    @Override
    public void broadcast(@NotNull String message) {
        Bukkit.broadcastMessage(message);
    }

    @Override
    public void logInfo(@NotNull String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void logWarning(@NotNull String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public @Nullable MPlayer getPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? new BukkitPlayer(player) : null;
    }

    @Override
    public @Nullable MPlayer getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        return player != null ? new BukkitPlayer(player) : null;
    }

    @Override
    public Collection<? extends MPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(BukkitPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable MWorld getWorld(String name) {
        org.bukkit.World world = Bukkit.getWorld(name);
        return world != null ? new BukkitWorld(world) : null;
    }

    @Override
    public Collection<? extends MWorld> getWorlds() {
        return Bukkit.getWorlds().stream()
                .map(BukkitWorld::new)
                .collect(Collectors.toList());
    }

    @Override
    public MScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public java.io.File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public void setWorldBorder(@NotNull String worldName, double centerX, double centerZ, double size) {
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world != null) {
            org.bukkit.WorldBorder border = world.getWorldBorder();
            border.setCenter(centerX, centerZ);
            border.setSize(size);
        }
    }

    @Override
    public void resetWorldBorder(@NotNull String worldName) {
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.getWorldBorder().reset();
        }
    }

    @Override
    public void spawnFireworks(@NotNull com.example.manhunt.api.MLocation location) {
        org.bukkit.World world = Bukkit.getWorld(location.getWorldName());
        if (world == null) return;

        org.bukkit.Location loc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ());
        org.bukkit.entity.Firework fw = world.spawn(loc, org.bukkit.entity.Firework.class);
        org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
        
        org.bukkit.FireworkEffect effect = org.bukkit.FireworkEffect.builder()
                .withColor(org.bukkit.Color.RED, org.bukkit.Color.GREEN, org.bukkit.Color.BLUE)
                .withFade(org.bukkit.Color.YELLOW)
                .with(org.bukkit.FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build();
        
        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    @Override
    public String getConfigString(String path, String def) {
        return plugin.getConfig().getString(path, def);
    }

    @Override
    public int getConfigInt(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }

    @Override
    public boolean getConfigBoolean(String path, boolean def) {
        return plugin.getConfig().getBoolean(path, def);
    }

    @Override
    public double getConfigDouble(String path, double def) {
        return plugin.getConfig().getDouble(path, def);
    }
}
