package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class SettingsMenu extends BaseMenu {

    public SettingsMenu(SpongeMain plugin) {
        super(plugin, "&8Genel Ayarlar", 3);
    }

    @Override
    protected void populate() {
        inventory.set(10, createItem(getItemType("REDSTONE_TORCH"), plugin.getGameManager().isHardcore() ? "&cHardcore: &aAÇIK" : "&cHardcore: &cKAPALI", "&7Ölen avcılar kalıcı olarak izleyici olur."));
        inventory.set(11, createItem(getItemType("LEVER"), plugin.getGameManager().isFFAMode() ? "&dHerkes Tek (FFA): &aAÇIK" : "&dHerkes Tek (FFA): &cKAPALI", "&7Herkes runner olur ve everyone birbirini takip edebilir."));
        inventory.set(12, createItem(getItemType("PAPER"), plugin.getGameManager().isAdvancementAnnouncementsEnabled() ? "&eBaşarı Duyuruları: &aAÇIK" : "&eBaşarı Duyuruları: &cKAPALI", "&7Başarıların herkese duyurulup duyurulmayacağını belirler."));
        inventory.set(13, createItem(getItemType("IRON_BARS"), "&fDünya Sınırı", "&7Border ayarlarını yönet."));
        inventory.set(14, createItem(getItemType("NETHER_STAR"), plugin.getGameManager().isBloodMoon() ? "&4Kanlı Ay: &aAÇIK" : "&4Kanlı Ay: &cKAPALI", "&7Geceleri moblar daha güçlü olur."));
        inventory.set(15, createItem(getItemType("GOLDEN_APPLE"), plugin.getGameManager().isClassSystemEnabled() ? "&6Sınıf Sistemi: &aAÇIK" : "&6Sınıf Sistemi: &cKAPALI", "&7Özel yetenek sınıflarını açar/kapatır."));
        inventory.set(16, createItem(getItemType("GHAST_TEAR"), plugin.getPlatform().getConfigBoolean("ghost-mode", true) ? "&bGhost Mode: &aAÇIK" : "&bGhost Mode: &cKAPALI", "&7Ölen avcılar 5s hayalet olur."));
        
        inventory.set(22, createItem(getItemType("ARROW"), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        switch (slotIndex) {
            case 10:
                plugin.getGameManager().setHardcore(!plugin.getGameManager().isHardcore());
                open(player);
                break;
            case 11:
                plugin.getGameManager().setFFAMode(!plugin.getGameManager().isFFAMode());
                open(player);
                break;
            case 12:
                plugin.getGameManager().setAdvancementAnnouncementsEnabled(!plugin.getGameManager().isAdvancementAnnouncementsEnabled());
                open(player);
                break;
            case 13:
                new WorldBorderMenu(plugin).open(player);
                break;
            case 14:
                plugin.getGameManager().setBloodMoon(!plugin.getGameManager().isBloodMoon());
                open(player);
                break;
            case 15:
                plugin.getGameManager().setClassSystemEnabled(!plugin.getGameManager().isClassSystemEnabled());
                open(player);
                break;
            case 16:
                // Sponge config saving depends on platform implementation
                boolean current = plugin.getPlatform().getConfigBoolean("ghost-mode", true);
                plugin.getPlatform().setConfigBoolean("ghost-mode", !current);
                open(player);
                break;
            case 22:
                new MainMenu(plugin).open(player);
                break;
        }
    }
}
