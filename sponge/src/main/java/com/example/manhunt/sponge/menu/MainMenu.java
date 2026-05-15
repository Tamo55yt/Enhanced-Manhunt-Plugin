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
        inventory.set(12, createItem(getItemType("IRON_SWORD"), "&6Sınıf Seçimi", "&7Özel yetenek sınıfı seçin."));
        inventory.set(13, createItem(getItemType("ANVIL"), "&aGenel Ayarlar", "&7Hardcore, FFA, Blood Moon vb. ayarlar."));
        inventory.set(14, createItem(getItemType("DIAMOND"), "&aOyunu Başlat", "&7Manhunt oyununu başlatır."));
        inventory.set(15, createItem(getItemType("BARRIER"), "&cOyunu Durdur", "&7Devam eden oyunu bitirir."));
        inventory.set(16, createItem(getItemType("CHEST"), "&eKit Ayarları", "&7Başlangıç eşyalarını düzenle."));
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
                new SettingsMenu(plugin).open(player); 
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
                plugin.getGameManager().endGame(new com.example.manhunt.sponge.impl.SpongePlayer(player).getLocation(), true);
                player.closeInventory();
                break;
            case 16:
                new KitMenu(plugin).open(player);
                break;
        }
    }
}
