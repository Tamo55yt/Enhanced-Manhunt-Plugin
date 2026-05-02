package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class MainMenu extends BaseMenu {

    public MainMenu(SpongeMain plugin) {
        super(plugin, "&8Manhunt Yönetim Paneli", 3);
    }

    @Override
    protected void populate() {
        inventory.set(10, createItem(getItemType("COMPASS"), "&bRunner Seçimi", "&7Runner ayarlamak için tıklayın."));
        inventory.set(11, createItem(getItemType("BOOK"), "&eSenaryolar", "&7Oyun modunu seçin."));
        inventory.set(12, createItem(getItemType("IRON_SWORD"), "&eSınıf Seçimi", "&7Özel yetenek sınıfı seçin."));
        inventory.set(13, createItem(getItemType("ANVIL"), "&6Kit Ayarları", "&7Kiti görsel olarak düzenleyin."));
        inventory.set(14, createItem(getItemType("DIAMOND"), "&aOyunu Başlat", "&7Manhunt oyununu başlatır."));
        inventory.set(15, createItem(getItemType("REDSTONE_TORCH"), plugin.getGameManager().isHardcore() ? "&cHardcore: &aAÇIK" : "&cHardcore: &cKAPALI", "&7Ölen avcılar kalıcı olarak izleyici olur."));
        inventory.set(16, createItem(getItemType("BARRIER"), "&cOyunu Durdur", "&7Devam eden oyunu bitirir."));
        inventory.set(17, createItem(getItemType("LEVER"), plugin.getGameManager().isFFAMode() ? "&dHerkes Tek (FFA): &aAÇIK" : "&dHerkes Tek (FFA): &cKAPALI", "&7Herkes runner olur ve everyone birbirini takip edebilir."));
        inventory.set(18, createItem(getItemType("PAPER"), plugin.getGameManager().isAdvancementAnnouncementsEnabled() ? "&eBaşarı Duyuruları: &aAÇIK" : "&eBaşarı Duyuruları: &cKAPALI", "&7Başarıların herkese duyurulup duyurulmayacağını belirler."));
        inventory.set(19, createItem(getItemType("GUNPOWDER"), "&fKaçan Yetenekleri", "&7Runner'a özel güçleri ayarla."));
        inventory.set(20, createItem(getItemType("IRON_BARS"), "&fDünya Sınırı", "&7Border ayarlarını yönet."));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        switch (slotIndex) {
            case 10: 
                new RunnerSelectionMenu(plugin).open(player); 
                break;
            case 11: 
                new ScenarioMenu(plugin).open(player); 
                break;
            case 12: 
                new ClassMenu(plugin).open(player); 
                break;
            case 13: 
                new KitMenu(plugin).open(player); 
                break;
            case 14:
                if (plugin.getGameManager().isGameRunning()) {
                    plugin.getMessageManager().sendMessage(new com.example.manhunt.sponge.impl.SpongePlayer(player), "game_already_running");
                } else {
                    plugin.getGameManager().startCountdown();
                    player.closeInventory();
                }
                break;
            case 15:
                plugin.getGameManager().setHardcore(!plugin.getGameManager().isHardcore());
                plugin.getMessageManager().broadcast(plugin.getGameManager().isHardcore() ? "hardcore_enabled" : "hardcore_disabled");
                open(player); // Refresh
                break;
            case 16:
                // Handle stop via game manager
                plugin.getGameManager().endGame(new com.example.manhunt.sponge.impl.SpongePlayer(player).getLocation(), true);
                player.closeInventory();
                break;
            case 17:
                plugin.getGameManager().setFFAMode(!plugin.getGameManager().isFFAMode());
                String msg = plugin.getGameManager().isFFAMode() ? "&6[Manhunt] &dHerkes Tek (FFA) Modu &aAKTİF!" : "&6[Manhunt] &dHerkes Tek (FFA) Modu &cKAPALI!";
                plugin.getMessageManager().broadcastRaw(msg);
                open(player); // Refresh
                break;
            case 18:
                boolean current = plugin.getGameManager().isAdvancementAnnouncementsEnabled();
                plugin.getGameManager().setAdvancementAnnouncementsEnabled(!current);
                String status = !current ? "&aAÇIK" : "&cKAPALI";
                plugin.getMessageManager().broadcastRaw("&6[Manhunt] &eBaşarı Duyuruları: " + status);
                open(player); // Refresh
                break;
            case 19:
                new RunnerAbilityMenu(plugin).open(player);
                break;
            case 20:
                new WorldBorderMenu(plugin).open(player);
                break;
        }
    }
}
