package com.example.manhunt.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;

public interface MPlatform {
    void broadcast(@NotNull String message);
    void logInfo(@NotNull String message);
    void logWarning(@NotNull String message);
    
    @Nullable MPlayer getPlayer(UUID uuid);
    @Nullable MPlayer getPlayer(String name);
    Collection<? extends MPlayer> getOnlinePlayers();
    
    @Nullable MWorld getWorld(String name);
    Collection<? extends MWorld> getWorlds();
    
    MScheduler getScheduler();
    
    java.io.File getDataFolder();
    
    // World Border
    void setWorldBorder(@NotNull String worldName, double centerX, double centerZ, double size);
    void resetWorldBorder(@NotNull String worldName);

    void spawnFireworks(@NotNull MLocation location);

    // Config access (simplified for now)
    String getConfigString(String path, String def);
    int getConfigInt(String path, int def);
    boolean getConfigBoolean(String path, boolean def);
    double getConfigDouble(String path, double def);
}
