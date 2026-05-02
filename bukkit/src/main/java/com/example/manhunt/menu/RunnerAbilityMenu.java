package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class RunnerAbilityMenu extends BaseMenu {

    public RunnerAbilityMenu(@NotNull Main plugin) {
        super(plugin, "&8Kaçan Yetenekleri", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        
        boolean smoke = plugin.getGameManager().isSmokeBombEnabled();
        inventory.setItem(13, createItem(getMaterial("GUNPOWDER"), 
            "&f&lSis Bombası (Smoke Bomb)", 
            "&7Sağ tıklandığında çevredeki",
            "&7avcılara körlük ve yavaşlık verir.",
            "&7",
            smoke ? "&aDURUM: AKTİF" : "&cDURUM: DEVRE DIŞI",
            "&eDeğiştirmek için tıkla."));

        inventory.setItem(26, createItem(getMaterial("ARROW"), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot == 13) {
            plugin.getGameManager().setSmokeBombEnabled(!plugin.getGameManager().isSmokeBombEnabled());
            open(player);
        } else if (slot == 26) {
            new MainMenu(plugin).open(player);
        }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
