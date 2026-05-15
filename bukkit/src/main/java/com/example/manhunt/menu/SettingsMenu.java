package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SettingsMenu extends BaseMenu {

    public SettingsMenu(@NotNull Main plugin) {
        super(plugin, "&8Genel Ayarlar", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();

        inventory.setItem(10, createItem(getMaterial("REDSTONE_TORCH"), plugin.getGameManager().isHardcore() ? "&cHardcore: &aAÇIK" : "&cHardcore: &cKAPALI", "&7Ölen avcılar kalıcı olarak izleyici olur."));
        inventory.setItem(11, createItem(getMaterial("LEVER"), plugin.getGameManager().isFFAMode() ? "&dHerkes Tek (FFA): &aAÇIK" : "&dHerkes Tek (FFA): &cKAPALI", "&7Herkes runner olur ve herkes birbirini takip edebilir."));
        inventory.setItem(12, createItem(Material.PAPER, plugin.getGameManager().isAdvancementAnnouncementsEnabled() ? "&eBaşarı Duyuruları: &aAÇIK" : "&eBaşarı Duyuruları: &cKAPALI", "&7Başarıların herkese duyurulup duyurulmayacağını belirler."));
        inventory.setItem(13, createItem(getMaterial("IRON_BARS"), "&fDünya Sınırı", "&7Border ayarlarını yönet."));
        inventory.setItem(14, createItem(getMaterial("NETHER_STAR"), plugin.getGameManager().isBloodMoon() ? "&4Kanlı Ay: &aAÇIK" : "&4Kanlı Ay: &cKAPALI", "&7Geceleri moblar daha güçlü olur."));
        inventory.setItem(15, createItem(getMaterial("GOLDEN_APPLE"), plugin.getGameManager().isClassSystemEnabled() ? "&6Sınıf Sistemi: &aAÇIK" : "&6Sınıf Sistemi: &cKAPALI", "&7Özel yetenek sınıflarını açar/kapatır."));
        inventory.setItem(16, createItem(getMaterial("GHAST_TEAR"), plugin.getConfig().getBoolean("ghost-mode", true) ? "&bGhost Mode: &aAÇIK" : "&bGhost Mode: &cKAPALI", "&7Ölen avcılar 5s hayalet olur."));
        
        inventory.setItem(22, createItem(getMaterial("ARROW"), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        if (slot == 22) {
            new MainMenu(plugin).open(player);
            return;
        }

        if (slot == 10) {
            plugin.getGameManager().setHardcore(!plugin.getGameManager().isHardcore());
            open(player);
        } else if (slot == 11) {
            plugin.getGameManager().setFFAMode(!plugin.getGameManager().isFFAMode());
            open(player);
        } else if (slot == 12) {
            plugin.getGameManager().setAdvancementAnnouncementsEnabled(!plugin.getGameManager().isAdvancementAnnouncementsEnabled());
            open(player);
        } else if (slot == 13) {
            new WorldBorderMenu(plugin).open(player);
        } else if (slot == 14) {
            plugin.getGameManager().setBloodMoon(!plugin.getGameManager().isBloodMoon());
            open(player);
        } else if (slot == 15) {
            plugin.getGameManager().setClassSystemEnabled(!plugin.getGameManager().isClassSystemEnabled());
            open(player);
        } else if (slot == 16) {
            boolean current = plugin.getConfig().getBoolean("ghost-mode", true);
            plugin.getConfig().set("ghost-mode", !current);
            plugin.saveConfig();
            open(player);
        }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
