package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.data.Keys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RunnerSelectionMenu extends BaseMenu {

    private final List<ServerPlayer> players;

    public RunnerSelectionMenu(SpongeMain plugin) {
        super(plugin, "&8Runner Seçiniz", 6);
        this.players = new ArrayList<>(Sponge.server().onlinePlayers());
    }

    @Override
    protected void populate() {
        for (int i = 0; i < players.size() && i < 53; i++) {
            ServerPlayer p = players.get(i);
            inventory.set(i, getPlayerHead(p));
        }
        
        inventory.set(53, createItem(getItemType("ANVIL"), "&eAyarları Düzenle", "&7Puan ve sayaç ayarları."));
    }

    private ItemStack getPlayerHead(ServerPlayer player) {
        boolean isRunner = plugin.getGameManager().getRunners().contains(player.uniqueId());
        ItemStack item = ItemStack.builder()
                .itemType(ItemTypes.PLAYER_HEAD)
                .add(Keys.DISPLAY_NAME, LegacyComponentSerializer.legacyAmpersand().deserialize("&b" + player.name()))
                .build();

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(isRunner ? "&aŞu an Runner" : "&cRunner Değil"));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Durumu değiştirmek için tıklayın."));
        item.offer(Keys.LORE, lore);
        
        // Apply skin profile
        player.get(Keys.SKIN_PROFILE_PROPERTY).ifPresent(skin -> item.offer(Keys.SKIN_PROFILE_PROPERTY, skin));
        
        return item;
    }

    @Override
    public void handleClick(ServerPlayer admin, Optional<Slot> slot, int slotIndex) {
        if (slotIndex == 53) {
            new SetupMenu(plugin).open(admin);
            return;
        }

        if (slotIndex >= 0 && slotIndex < players.size()) {
            ServerPlayer target = players.get(slotIndex);
            if (plugin.getGameManager().getRunners().contains(target.uniqueId())) {
                plugin.getGameManager().removeRunner(target.uniqueId());
                plugin.getMessageManager().sendMessage(new com.example.manhunt.sponge.impl.SpongePlayer(admin), "runner_removed", target.name());
            } else {
                plugin.getGameManager().addRunner(target.uniqueId());
                plugin.getMessageManager().sendMessage(new com.example.manhunt.sponge.impl.SpongePlayer(admin), "runner_added", target.name());
            }
            open(admin);
        }
    }
}
