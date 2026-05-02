package com.example.manhunt.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface Menu extends InventoryHolder {
    void handleClick(@NotNull InventoryClickEvent event);
}
