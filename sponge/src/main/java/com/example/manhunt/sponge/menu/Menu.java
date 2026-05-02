package com.example.manhunt.sponge.menu;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Slot;
import java.util.Optional;

public interface Menu {
    void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex);
}
