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
        inventory.setItem(12, createItem(getMaterial("IRON_SWORD"), "&eSınıf Seçimi", "&7Özel yetenek sınıfı seçin."));
        inventory.setItem(13, createItem(getMaterial("ANVIL"), "&6Kit Ayarları", "&7Kiti görsel olarak düzenleyin."));
        inventory.setItem(14, createItem(getMaterial("DIAMOND"), "&aOyunu Başlat", "&7Manhunt oyununu başlatır."));
        inventory.setItem(15, createItem(getMaterial("REDSTONE_TORCH"), plugin.getGameManager().isHardcore() ? "&cHardcore: &aAÇIK" : "&cHardcore: &cKAPALI", "&7Ölen avcılar kalıcı olarak izleyici olur."));
        inventory.setItem(16, createItem(getMaterial("BARRIER"), "&cOyunu Durdur", "&7Devam eden oyunu bitirir."));
        inventory.setItem(17, createItem(getMaterial("LEVER"), plugin.getGameManager().isFFAMode() ? "&dHerkes Tek (FFA): &aAÇIK" : "&dHerkes Tek (FFA): &cKAPALI", "&7Herkes runner olur ve herkes birbirini takip edebilir."));
        inventory.setItem(18, createItem(Material.PAPER, plugin.getGameManager().isAdvancementAnnouncementsEnabled() ? "&eBaşarı Duyuruları: &aAÇIK" : "&eBaşarı Duyuruları: &cKAPALI", "&7Başarıların herkese duyurulup duyurulmayacağını belirler."));
        inventory.setItem(19, createItem(getMaterial("GUNPOWDER"), "&fKaçan Yetenekleri", "&7Runner'a özel güçleri ayarla."));
        inventory.setItem(20, createItem(getMaterial("IRON_BARS"), "&fDünya Sınırı", "&7Border ayarlarını yönet."));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        if (slot == 19) {
            new RunnerAbilityMenu(plugin).open(player);
            return;
        } else if (slot == 20) {
            new WorldBorderMenu(plugin).open(player);
            return;
        }

        String typeName = item.getType().name();
        if (typeName.equals("COMPASS")) {
            new RunnerSelectionMenu(plugin).open(player);
        } else if (typeName.equals("BOOK")) {
            new ScenarioMenu(plugin).open(player);
        } else if (typeName.equals("IRON_SWORD")) {
            new ClassMenu(plugin).open(player);
        } else if (typeName.equals("ANVIL")) {
            new KitMenu(plugin).open(player);
        } else if (typeName.equals("REDSTONE_TORCH")) {
            plugin.getGameManager().setHardcore(!plugin.getGameManager().isHardcore());
            plugin.getMessageManager().broadcast(plugin.getGameManager().isHardcore() ? "hardcore_enabled" : "hardcore_disabled");
            open(player); // Refresh
        } else if (typeName.equals("DIAMOND")) {
            player.closeInventory();
            if (plugin.getGameManager().isGameRunning()) {
                plugin.getMessageManager().sendMessage(player, "game_already_running");
            } else {
                plugin.getGameManager().startCountdown();
            }
        } else if (typeName.equals("BARRIER")) {
            player.performCommand("manhunt stop");
            player.closeInventory();
        } else if (typeName.equals("LEVER")) {
            plugin.getGameManager().setFFAMode(!plugin.getGameManager().isFFAMode());
            String msg = plugin.getGameManager().isFFAMode() ? "&6[Manhunt] &dHerkes Tek (FFA) Modu &aAKTİF!" : "&6[Manhunt] &dHerkes Tek (FFA) Modu &cKAPALI!";
            plugin.getMessageManager().broadcastRaw(msg);
            open(player); // Refresh
        } else if (typeName.equals("PAPER")) {
            boolean current = plugin.getGameManager().isAdvancementAnnouncementsEnabled();
            plugin.getGameManager().setAdvancementAnnouncementsEnabled(!current);
            String status = !current ? "&aAÇIK" : "&cKAPALI";
            plugin.getMessageManager().broadcastRaw("&6[Manhunt] &eBaşarı Duyuruları: " + status);
            open(player); // Refresh
        }
    }

    public void open(Player player) {
        createInventory(); // Ensure it's fresh (especially for toggles)
        player.openInventory(getInventory());
    }
}
