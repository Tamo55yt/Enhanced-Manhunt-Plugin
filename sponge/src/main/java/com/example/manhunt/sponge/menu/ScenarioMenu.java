package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class ScenarioMenu extends BaseMenu {

    public ScenarioMenu(SpongeMain plugin) {
        super(plugin, "&8Senaryo Seçimi", 3);
    }

    @Override
    protected void populate() {
        inventory.set(10, createItem(ItemTypes.GRASS_BLOCK.get(), "&fNormal Manhunt", "&7Standart oyun modu."));
        inventory.set(12, createItem(ItemTypes.LAVA_BUCKET.get(), "&cLav Yükseliyor", "&7Her 2 dakikada bir lav seviyesi artar."));
        inventory.set(14, createItem(ItemTypes.CHEST.get(), "&6Rastgele Eşya", "&7Bloklardan rastgele eşya düşer."));
        inventory.set(16, createItem(ItemTypes.ENDER_PEARL.get(), "&dYer Değiştirme", "&7Her 10 dakikada bir yerler değişir."));
        inventory.set(22, createItem(ItemTypes.GREEN_STAINED_GLASS_PANE.get(), "&aGeri Dön", ""));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (slotIndex == 22) {
            new MainMenu(plugin).open(player);
            return;
        }

        switch (slotIndex) {
            case 10: plugin.getScenarioManager().setSelectedScenario("NORMAL"); break;
            case 12: plugin.getScenarioManager().setSelectedScenario("LAVA"); break;
            case 14: plugin.getScenarioManager().setSelectedScenario("RANDOM_DROPS"); break;
            case 16: plugin.getScenarioManager().setSelectedScenario("SWAP"); break;
            default: return;
        }

        player.sendMessage(net.kyori.adventure.text.Component.text("§aSenaryo Seçildi!"));
        player.closeInventory();
    }
}
