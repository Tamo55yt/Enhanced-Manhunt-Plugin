package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class WorldBorderMenu extends BaseMenu {

    public WorldBorderMenu(@NotNull Main plugin) {
        super(plugin, "&8Dünya Sınırı Ayarları", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        
        int type = plugin.getGameManager().getBorderType();

        inventory.setItem(10, createItem(getMaterial("BARRIER"), "&fSınır Yok", type == 0 ? "&aSEÇİLİ" : "&7Sonsuz dünya."));
        inventory.setItem(13, createItem(getMaterial("IRON_BARS"), "&fSabit Sınır (5000)", type == 1 ? "&aSEÇİLİ" : "&7Belirli bir çapta sınır."));
        inventory.setItem(16, createItem(getMaterial("COMPASS"), "&fDinamik Sınır", type == 2 ? "&aSEÇİLİ" : "&7Runner uzaklaşırsa daralan sınır."));

        inventory.setItem(26, createItem(getMaterial("ARROW"), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 10) { plugin.getGameManager().setBorderType(0); open(player); }
        else if (slot == 13) { plugin.getGameManager().setBorderType(1); open(player); }
        else if (slot == 16) { plugin.getGameManager().setBorderType(2); open(player); }
        else if (slot == 26) { new MainMenu(plugin).open(player); }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
