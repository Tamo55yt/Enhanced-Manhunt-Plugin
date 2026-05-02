package com.example.manhunt.api;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public interface MWorld {
    @NotNull String getName();
    int getMaxHeight();
    void setBlock(int x, int y, int z, String materialName);
    String getBlockType(int x, int y, int z);
    MLocation getSpawnLocation();
    boolean isInsideBorder(MLocation location);
    
    Collection<? extends MPlayer> getPlayers();
}
