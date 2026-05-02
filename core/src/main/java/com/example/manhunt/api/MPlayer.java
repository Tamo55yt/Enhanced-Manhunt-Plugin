package com.example.manhunt.api;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public interface MPlayer {
    @NotNull String getName();
    @NotNull UUID getUniqueId();
    void sendMessage(@NotNull String message);
    void sendActionBar(@NotNull String message);
    void sendTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut);
    
    MLocation getLocation();
    void teleport(MLocation location);
    void setCompassTarget(MLocation location);
    void updateInventoryCompass(MLocation target);
    
    double getHealth();
    void setHealth(double health);
    void setMaxHealth(double health);
    
    void clearInventory();
    void giveItem(@NotNull String material, int amount);
    void addPotionEffect(@NotNull String effectType, int duration, int amplifier);
    void setGameMode(String mode);
    
    boolean isOnline();
    boolean equals(Object obj);
}
