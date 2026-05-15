package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class ClassMenu extends BaseMenu {

    public ClassMenu(SpongeMain plugin) {
        super(plugin, "&8Sınıf Seçimi", 3);
    }

    @Override
    protected void populate() {
        inventory.set(10, createItem(ItemTypes.FEATHER.get(), "&aİzci (Scout)", "&7Hız I verir, ancak 8 kalbin olur.", "&7Yetenek: Leap (Büyük Zıplama)"));
        inventory.set(12, createItem(ItemTypes.DIAMOND_AXE.get(), "&4Berserker", "&7Kuvvet I verir, ancak %15 yavaşlatır.", "&7Yetenek: Decoy (Sahte Kopya)"));
        inventory.set(14, createItem(ItemTypes.IRON_CHESTPLATE.get(), "&cTank", "&7Direnç I verir, ancak Yavaşlık I verir.", "&712 kalbin olur."));
        inventory.set(16, createItem(ItemTypes.TRIPWIRE_HOOK.get(), "&6Tuzakçı (Trapper)", "&7Örümcek ağı ve TNT ile başlar."));
        inventory.set(22, createItem(ItemTypes.GREEN_STAINED_GLASS_PANE.get(), "&aGeri Dön", ""));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (!slot.isPresent()) return;

        if (slotIndex == 22) {
            new MainMenu(plugin).open(player);
            return;
        }

        switch (slotIndex) {
            case 10: plugin.getGameManager().setPlayerClass(player.uniqueId(), "SCOUT"); break;
            case 12: plugin.getGameManager().setPlayerClass(player.uniqueId(), "BERSERKER"); break;
            case 14: plugin.getGameManager().setPlayerClass(player.uniqueId(), "TANK"); break;
            case 16: plugin.getGameManager().setPlayerClass(player.uniqueId(), "TRAPPER"); break;
            default: return;
        }

        player.sendMessage(net.kyori.adventure.text.Component.text("§aSınıf Seçildi!"));
        player.closeInventory();
    }
}
