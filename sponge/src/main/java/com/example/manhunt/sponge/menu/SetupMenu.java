package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SetupMenu extends BaseMenu {

    public static final Map<UUID, String> awaitingChatInput = new HashMap<>();

    public SetupMenu(SpongeMain plugin) {
        super(plugin, "&8Sayaç / Hedef Ayarı", 3);
    }

    @Override
    protected void populate() {
        int target = plugin.getPlatform().getConfigInt("tournament.target-score", 10);
        int mins = plugin.getPlatform().getConfigInt("timer.countdown-minutes", 60);

        inventory.set(11, createItem(ItemTypes.OAK_SIGN.get(), "&eGeri Sayım Süresi", "&7Mevcut: &a" + mins + " dk", "&7Tıklayıp chat'e yeni süreyi yazın."));
        inventory.set(15, createItem(ItemTypes.OAK_SIGN.get(), "&bHedef Puan", "&7Mevcut: &a" + target + " puan", "&7Tıklayıp chat'e yeni puanı yazın."));

        inventory.set(21, createItem(ItemTypes.GREEN_STAINED_GLASS_PANE.get(), "&aGeri Dön", ""));
        inventory.set(23, createItem(ItemTypes.RED_STAINED_GLASS_PANE.get(), "&cMenüyü Kapat", ""));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (!slot.isPresent()) return;

        if (slotIndex == 21) {
            new MainMenu(plugin).open(player);
            return;
        } else if (slotIndex == 23) {
            player.closeInventory();
            return;
        }

        if (slotIndex == 11) {
            awaitingChatInput.put(player.uniqueId(), "countdown");
            player.closeInventory();
            player.sendMessage(net.kyori.adventure.text.Component.text("§aLütfen chat'e yeni geri sayım süresini (dakika cinsinden) yazın:"));
        } else if (slotIndex == 15) {
            awaitingChatInput.put(player.uniqueId(), "target");
            player.closeInventory();
            player.sendMessage(net.kyori.adventure.text.Component.text("§aLütfen chat'e yeni hedef puanı yazın:"));
        }
    }
}
