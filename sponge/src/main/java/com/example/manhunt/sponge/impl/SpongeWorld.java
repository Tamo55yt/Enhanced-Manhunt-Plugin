package com.example.manhunt.sponge.impl;

import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collection;
import java.util.stream.Collectors;

public class SpongeWorld implements MWorld {
    private final ServerWorld world;

    public SpongeWorld(ServerWorld world) {
        this.world = world;
    }

    @Override
    public @NotNull String getName() {
        return world.key().toString();
    }

    @Override
    public int getMaxHeight() {
        return world.maximumHeight();
    }

    @Override
    public void setBlock(int x, int y, int z, String materialName) {
        String key = materialName.contains(":") ? materialName.toLowerCase() : "minecraft:" + materialName.toLowerCase();
        org.spongepowered.api.Sponge.game().registry(org.spongepowered.api.registry.RegistryTypes.BLOCK_TYPE)
                .findValue(org.spongepowered.api.ResourceKey.resolve(key))
                .ifPresent(type -> world.setBlock(x, y, z, type.defaultState()));
    }

    @Override
    public String getBlockType(int x, int y, int z) {
        return world.block(x, y, z).type().key(RegistryTypes.BLOCK_TYPE).toString();
    }

    @Override
    public MLocation getSpawnLocation() {
        return new MLocation(getName(), 0, 64, 0, 0, 0); // Simplified
    }

    @Override
    public boolean isInsideBorder(MLocation location) {
        return true; // Simplified
    }

    @Override
    public Collection<? extends MPlayer> getPlayers() {
        return world.players().stream().map(SpongePlayer::new).collect(Collectors.toList());
    }
}
