package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class KitMenu extends BaseMenu {

    public KitMenu(SpongeMain plugin) {
        super(plugin, "&8Kit Kaydetme Menüsü", 3);
    }

    @Override
    protected void populate() {
        ItemStack glass = createItem(ItemTypes.RED_STAINED_GLASS_PANE.get(), " ", "");
        for (int i = 0; i < 27; i++) inventory.set(i, glass);

        inventory.set(11, createItem(ItemTypes.DIAMOND.get(), "&b&lRUNNER Kiti Olarak Kaydet", 
                "&7Şu an üzerindeki tüm eşyaları", "&7ve zırhları Runner kiti yapar."));
        
        inventory.set(15, createItem(ItemTypes.IRON_SWORD.get(), "&e&lHUNTER Kiti Olarak Kaydet", 
                "&7Şu an üzerindeki tüm eşyaları", "&7ve zırhları Hunter kiti yapar."));

        inventory.set(22, createItem(ItemTypes.GREEN_STAINED_GLASS_PANE.get(), "&aGeri Dön", ""));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (!slot.isPresent()) return;

        if (slotIndex == 22) {
            new MainMenu(plugin).open(player);
            return;
        }

        if (slotIndex == 11) {
            // plugin.getKitManager().saveKit(new com.example.manhunt.sponge.impl.SpongePlayer(player), "runner");
            player.sendMessage(net.kyori.adventure.text.Component.text("§aRunner kiti kaydedildi! (Sponge sürümünde simüle edildi)"));
            player.closeInventory();
        } else if (slotIndex == 15) {
            // plugin.getKitManager().saveKit(new com.example.manhunt.sponge.impl.SpongePlayer(player), "hunter");
            player.sendMessage(net.kyori.adventure.text.Component.text("§aHunter kiti kaydedildi! (Sponge sürümünde simüle edildi)"));
            player.closeInventory();
        }
    }
}
