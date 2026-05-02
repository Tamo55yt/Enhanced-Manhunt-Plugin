package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;

public class RunnerAbilityMenu extends BaseMenu {

    public RunnerAbilityMenu(SpongeMain plugin) {
        super(plugin, "&8Kaçan Yetenekleri", 3);
    }

    @Override
    protected void populate() {
        boolean smoke = plugin.getGameManager().isSmokeBombEnabled();
        inventory.set(13, createItem(ItemTypes.GUNPOWDER.get(), 
            "&f&lSis Bombası (Smoke Bomb)", 
            "&7Sağ tıklandığında çevredeki",
            "&7avcılara körlük ve yavaşlık verir.",
            "&7",
            smoke ? "&aDURUM: AKTİF" : "&cDURUM: DEVRE DIŞI",
            "&eDeğiştirmek için tıkla."));

        inventory.set(26, createItem(ItemTypes.ARROW.get(), "&cGERİ DÖN", "&7Ana menüye döner."));
    }

    @Override
    public void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex) {
        if (slotIndex == 13) {
            plugin.getGameManager().setSmokeBombEnabled(!plugin.getGameManager().isSmokeBombEnabled());
            open(player);
        } else if (slotIndex == 26) {
            new MainMenu(plugin).open(player);
        }
    }
}
