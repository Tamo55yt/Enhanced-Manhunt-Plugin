package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class MenuManager implements Listener {

    public MenuManager(@NotNull Main plugin) {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Menu) {
            event.setCancelled(true);
            ((Menu) holder).handleClick(event);
        } else {
            // Check top inventory for cases where the holder is in the top but click is in bottom
            InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
            if (topHolder instanceof Menu) {
                // Usually we want to cancel clicks in the player's inventory too if a custom menu is open
                // unless specifically allowed. For now, let's keep it simple.
                if (event.getRawSlot() < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    ((Menu) topHolder).handleClick(event);
                }
            }
        }
    }
}
