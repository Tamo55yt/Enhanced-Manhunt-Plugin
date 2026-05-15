package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MainMenu extends BaseMenu {

    public MainMenu(@NotNull Main plugin) {
        super(plugin, "&8Manhunt Yönetim Paneli", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();

        inventory.setItem(10, createItem(getMaterial("COMPASS"), "&bRunner Seçimi", "&7Runner ayarlamak için tıklayın."));
        inventory.setItem(11, createItem(getMaterial("BOOK"), "&eSenaryolar", "&7Oyun modunu seçin."));
        inventory.setItem(12, createItem(getMaterial("IRON_SWORD"), "&6Sınıf Seçimi", "&7Özel yetenek sınıfı seçin."));
        inventory.setItem(13, createItem(getMaterial("ANVIL"), "&aGenel Ayarlar", "&7Hardcore, FFA, Blood Moon vb. ayarlar."));
        inventory.setItem(14, createItem(getMaterial("DIAMOND"), "&aOyunu Başlat", "&7Manhunt oyununu başlatır."));
        inventory.setItem(15, createItem(getMaterial("BARRIER"), "&cOyunu Durdur", "&7Devam eden oyunu bitirir."));
        inventory.setItem(16, createItem(getMaterial("CHEST"), "&eKit Ayarları", "&7Başlangıç eşyalarını düzenle."));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        if (slot == 10) {
            new RunnerSelectionMenu(plugin).open(player);
        } else if (slot == 11) {
            new ScenarioMenu(plugin).open(player);
        } else if (slot == 12) {
            new ClassMenu(plugin).open(player);
        } else if (slot == 13) {
            new SettingsMenu(plugin).open(player);
        } else if (slot == 14) {
            player.closeInventory();
            if (plugin.getGameManager().isGameRunning()) {
                plugin.getMessageManager().sendMessage(player, "game_already_running");
            } else {
                plugin.getGameManager().startCountdown();
            }
        } else if (slot == 15) {
            player.performCommand("manhunt stop");
            player.closeInventory();
        } else if (slot == 16) {
            new KitMenu(plugin).open(player);
        }
    }

    public void open(Player player) {
        createInventory(); // Ensure it's fresh (especially for toggles)
        player.openInventory(getInventory());
    }
}
