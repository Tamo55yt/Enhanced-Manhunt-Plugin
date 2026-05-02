package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class WorldBorderMenu extends BaseMenu {

    public WorldBorderMenu(SpongeMain plugin) {
        super(plugin, "&8Dünya Sınırı Ayarları", 3);
    }

    @Override
    protected void populate() {
        int type = plugin.getGameManager().getBorderType();

        inventory.set(10, createItem(ItemTypes.BARRIER.get(), "&fSınır Yok", type == 0 ? "&aSEÇİLİ" : "&7Sonsuz dünya."));
        inventory.set(13, createItem(ItemTypes.IRON_BARS.get(), "&fSabit Sınır (5000)", type == 1 ? "&aSEÇİLİ" : "&7Belirli bir çapta sınır."));
        inventory.set(16, createItem(ItemTypes.COMPASS.get(), "&fDinamik Sınır", type == 2 ? "&aSEÇİLİ" : "&7Runner uzaklaşırsa daralan sınır."));

        inventory.set(26, createItem(ItemTypes.ARROW.get(), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (slotIndex == 10) { plugin.getGameManager().setBorderType(0); open(player); }
        else if (slotIndex == 13) { plugin.getGameManager().setBorderType(1); open(player); }
        else if (slotIndex == 16) { plugin.getGameManager().setBorderType(2); open(player); }
        else if (slotIndex == 26) { new MainMenu(plugin).open(player); }
    }
}
