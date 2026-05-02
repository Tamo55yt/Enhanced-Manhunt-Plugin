package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class KitMenu extends BaseMenu {

    public KitMenu(@NotNull Main plugin) {
        super(plugin, "&8Kit Kaydetme Menüsü", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();

        // Background
        ItemStack glass = createGlass(false, " ");
        for (int i = 0; i < 27; i++) inventory.setItem(i, glass);

        inventory.setItem(11, createItem(Material.DIAMOND, "&b&lRUNNER Kiti Olarak Kaydet", 
                "&7Şu an üzerindeki tüm eşyaları", "&7ve zırhları Runner kiti yapar."));
        
        inventory.setItem(15, createItem(Material.IRON_SWORD, "&e&lHUNTER Kiti Olarak Kaydet", 
                "&7Şu an üzerindeki tüm eşyaları", "&7ve zırhları Hunter kiti yapar."));

        inventory.setItem(22, createGlass(true, "&aGeri Dön"));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.DIAMOND) {
            plugin.getKitManager().saveKit(new com.example.manhunt.bukkit.impl.BukkitPlayer(player), "runner");
            player.closeInventory();
        } else if (item.getType() == Material.IRON_SWORD) {
            plugin.getKitManager().saveKit(new com.example.manhunt.bukkit.impl.BukkitPlayer(player), "hunter");
            player.closeInventory();
        }
 else if (item.getType().name().contains("STAINED_GLASS_PANE")) {
            if (ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Geri Dön")) {
                new MainMenu(plugin).open(player);
            }
        }
    }

    public void open(Player player) {
        player.openInventory(getInventory());
    }
}
