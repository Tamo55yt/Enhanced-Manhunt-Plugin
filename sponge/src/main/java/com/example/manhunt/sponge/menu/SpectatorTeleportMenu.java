package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.sponge.impl.SpongePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Slot;

import java.util.Optional;
import java.util.UUID;

public class SpectatorTeleportMenu extends BaseMenu {

    public SpectatorTeleportMenu(SpongeMain plugin) {
        super(plugin, "&8Oyuncu Işınlanma", 3);
    }

    @Override
    protected void populate() {
        int slot = 0;
        
        // Add Runners
        for (UUID uuid : plugin.getGameManager().getRunners()) {
            Optional<ServerPlayer> runner = Sponge.server().player(uuid);
            if (runner.isPresent()) {
                inventory.set(slot++, createItem(ItemTypes.PLAYER_HEAD.get(), "&b&lRUNNER: &f" + runner.get().name(), "&7Tıklayarak ışınlan."));
                if (slot >= 27) break;
            }
        }

        // Add Alive Hunters
        if (slot < 27) {
            for (ServerPlayer p : Sponge.server().onlinePlayers()) {
                if (plugin.getGameManager().isHunter(new SpongePlayer(p)) && !plugin.getGameManager().getDeadHunters().contains(p.uniqueId())) {
                    inventory.set(slot++, createItem(ItemTypes.PLAYER_HEAD.get(), "&6&lHUNTER: &f" + p.name(), "&7Tıklayarak ışınlan."));
                    if (slot >= 27) break;
                }
            }
        }
    }

    @Override
    public void handleClick(ServerPlayer spectator, Optional<Slot> slot, int slotIndex) {
        if (!slot.isPresent()) return;
        
        Optional<Component> optName = slot.get().peek().get(org.spongepowered.api.data.Keys.DISPLAY_NAME);
        if (!optName.isPresent()) return;

        String name = PlainTextComponentSerializer.plainText().serialize(optName.get());
        if (name.contains(": ")) {
            String targetName = name.split(": ")[1];
            Optional<ServerPlayer> target = Sponge.server().player(targetName);
            if (target.isPresent()) {
                spectator.setLocation(target.get().serverLocation());
                spectator.sendMessage(Component.text(target.get().name() + " yanına ışınlandın.", NamedTextColor.GREEN));
                spectator.closeInventory();
            }
        }
    }
}
