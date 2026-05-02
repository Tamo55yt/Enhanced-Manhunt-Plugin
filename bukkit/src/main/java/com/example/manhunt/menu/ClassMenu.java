package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClassMenu extends BaseMenu {

    public ClassMenu(@NotNull Main plugin) {
        super(plugin, "&8Sınıf Seçimi", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        inventory.setItem(10, createItem(Material.FEATHER, "&aİzci (Scout)", "&7Hız I verir, ancak 8 kalbin olur."));
        inventory.setItem(13, createItem(Material.IRON_CHESTPLATE, "&cTank", "&7Direnç I verir, ancak Yavaşlık I verir.", "&712 kalbin olur."));
        inventory.setItem(16, createItem(Material.TRIPWIRE_HOOK, "&6Tuzakçı (Trapper)", "&7Örümcek ağı ve TNT ile başlar."));
        inventory.setItem(22, createGlass(true, "&aGeri Dön"));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name.contains("Geri Dön")) {
            new MainMenu(plugin).open(player);
            return;
        }

        if (item.getType() == Material.FEATHER) plugin.getScenarioManager().setPlayerClass(player.getUniqueId(), "SCOUT");
        else if (item.getType() == Material.IRON_CHESTPLATE) plugin.getScenarioManager().setPlayerClass(player.getUniqueId(), "TANK");
        else if (item.getType() == Material.TRIPWIRE_HOOK) plugin.getScenarioManager().setPlayerClass(player.getUniqueId(), "TRAPPER");

        player.sendMessage(ChatColor.GREEN + "Sınıf Seçildi: " + item.getItemMeta().getDisplayName());
        player.closeInventory();
    }

    public void open(Player player) {
        player.openInventory(getInventory());
    }
}
