package com.example.manhunt.bukkit.impl;

import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class BukkitWorld implements MWorld {
    private final World world;

    public BukkitWorld(World world) {
        this.world = world;
    }

    @Override
    public @NotNull String getName() {
        return world.getName();
    }

    @Override
    public int getMaxHeight() {
        return world.getMaxHeight();
    }

    @Override
    public void setBlock(int x, int y, int z, String materialName) {
        try {
            world.getBlockAt(x, y, z).setType(Material.valueOf(materialName.toUpperCase()));
        } catch (Exception ignored) {}
    }

    @Override
    public String getBlockType(int x, int y, int z) {
        return world.getBlockAt(x, y, z).getType().name();
    }

    @Override
    public MLocation getSpawnLocation() {
        Location loc = world.getSpawnLocation();
        return new MLocation(world.getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @Override
    public boolean isInsideBorder(MLocation location) {
        return world.getWorldBorder().isInside(new Location(world, location.getX(), location.getY(), location.getZ()));
    }

    @Override
    public Collection<? extends MPlayer> getPlayers() {
        return world.getPlayers().stream().map(BukkitPlayer::new).collect(Collectors.toList());
    }
}
