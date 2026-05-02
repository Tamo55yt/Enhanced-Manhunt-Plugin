package com.example.manhunt.bukkit;

import com.example.manhunt.Main;
import com.example.manhunt.api.MLocation;
import com.example.manhunt.bukkit.impl.BukkitPlayer;
import com.example.manhunt.manager.CompassManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BukkitCompassManager extends CompassManager {

    public BukkitCompassManager(Main plugin) {
        super(plugin.getPlatform(), plugin.getGameManager(), plugin.getMessageManager());
    }

    public void giveCompass(@NotNull Player player) {
        boolean hasCompass = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                hasCompass = true;
                break;
            }
        }
        
        if (!hasCompass) {
            player.getInventory().addItem(new ItemStack(Material.COMPASS));
        }
        getCompassModes().putIfAbsent(player.getUniqueId(), 0);
    }

    public void cycleCompassMode(Player player) {
        super.cycleCompassMode(new BukkitPlayer(player));
    }

    public void setLastPortalLocation(Location loc) {
        super.setLastPortalLocation(new MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
    }
}
