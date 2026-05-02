package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMenu implements Menu {

    protected final Main plugin;
    protected final String title;
    protected final int size;
    protected Inventory inventory;

    public BaseMenu(@NotNull Main plugin, String title, int size) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = size;
    }

    protected void createInventory() {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        if (inventory == null) createInventory();
        return inventory;
    }

    protected ItemStack createItem(Material mat, String name, String... lore) {
        return createItem(mat, (short) 0, name, lore);
    }

    @SuppressWarnings("deprecation")
    protected ItemStack createItem(Material mat, short data, String name, String... lore) {
        ItemStack item = new ItemStack(mat, 1, data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            for (String l : lore) coloredLore.add(ChatColor.translateAlternateColorCodes('&', l));
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createGlass(boolean green, String name) {
        Material mat = Material.getMaterial("GREEN_STAINED_GLASS_PANE");
        if (mat == null) {
            mat = Material.getMaterial("STAINED_GLASS_PANE"); // 1.12 Legacy
            return createItem(mat, green ? (short) 5 : (short) 14, name);
        } else {
            return createItem(Material.getMaterial(green ? "GREEN_STAINED_GLASS_PANE" : "RED_STAINED_GLASS_PANE"), (short) 0, name);
        }
    }

    protected Material getMaterial(String... names) {
        for (String n : names) {
            Material m = Material.getMaterial(n);
            if (m != null) return m;
        }
        return Material.STONE;
    }
}
