package com.example.manhunt.sponge.menu;

import com.example.manhunt.SpongeMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseMenu {

    protected final SpongeMain plugin;
    protected final Component title;
    protected final int rows;
    protected ViewableInventory inventory;
    protected InventoryMenu menu;

    public BaseMenu(SpongeMain plugin, String title, int rows) {
        this.plugin = plugin;
        this.title = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
        this.rows = rows;
    }

    protected void createInventory() {
        this.inventory = ViewableInventory.builder()
                .type(getContainerType(rows))
                .completeStructure()
                .plugin(plugin.getContainer())
                .build();
        
        this.menu = inventory.asMenu();
        this.menu.setTitle(title);
        this.menu.setReadOnly(true);
        
        // Native Sponge 8+ click handler
        this.menu.registerSlotClick((cause, container, slot, slotIndex, clickType) -> {
            Optional<ServerPlayer> player = cause.first(ServerPlayer.class);
            if (player.isPresent()) {
                handleClick(player.get(), Optional.ofNullable(slot), slotIndex);
            }
            return false;
        });
    }

    public ViewableInventory getInventory() {
        return inventory;
    }

    private org.spongepowered.api.item.inventory.ContainerType getContainerType(int rows) {
        switch (rows) {
            case 1: return ContainerTypes.GENERIC_9X1.get();
            case 2: return ContainerTypes.GENERIC_9X2.get();
            case 3: return ContainerTypes.GENERIC_9X3.get();
            case 4: return ContainerTypes.GENERIC_9X4.get();
            case 5: return ContainerTypes.GENERIC_9X5.get();
            case 6: return ContainerTypes.GENERIC_9X6.get();
            default: return ContainerTypes.GENERIC_9X3.get();
        }
    }

    public void open(ServerPlayer player) {
        if (inventory == null) {
            createInventory();
        }
        populate(); // Always populate before opening or refreshing
        menu.open(player);
    }

    protected abstract void populate();

    public abstract void handleClick(ServerPlayer player, Optional<Slot> slot, int slotIndex);

    protected ItemStack createItem(ItemType type, String name, String... lore) {
        ItemStack item = ItemStack.builder()
                .itemType(type)
                .add(Keys.DISPLAY_NAME, LegacyComponentSerializer.legacyAmpersand().deserialize(name))
                .build();
        
        if (lore.length > 0) {
            List<Component> components = new ArrayList<>();
            for (String l : lore) {
                components.add(LegacyComponentSerializer.legacyAmpersand().deserialize(l));
            }
            item.offer(Keys.LORE, components);
        }
        return item;
    }

    protected ItemType getItemType(String name) {
        switch (name.toUpperCase()) {
            case "COMPASS": return ItemTypes.COMPASS.get();
            case "BOOK": return ItemTypes.BOOK.get();
            case "IRON_SWORD": return ItemTypes.IRON_SWORD.get();
            case "ANVIL": return ItemTypes.ANVIL.get();
            case "DIAMOND": return ItemTypes.DIAMOND.get();
            case "REDSTONE_TORCH": return ItemTypes.REDSTONE_TORCH.get();
            case "BARRIER": return ItemTypes.BARRIER.get();
            case "LEVER": return ItemTypes.LEVER.get();
            case "GUNPOWDER": return ItemTypes.GUNPOWDER.get();
            case "IRON_BARS": return ItemTypes.IRON_BARS.get();
            case "GREEN_STAINED_GLASS_PANE": return ItemTypes.GREEN_STAINED_GLASS_PANE.get();
            case "RED_STAINED_GLASS_PANE": return ItemTypes.RED_STAINED_GLASS_PANE.get();
            case "OAK_SIGN": return ItemTypes.OAK_SIGN.get();
            case "GRASS": return ItemTypes.GRASS_BLOCK.get();
            case "GRASS_BLOCK": return ItemTypes.GRASS_BLOCK.get();
            case "CHEST": return ItemTypes.CHEST.get();
            case "ENDER_PEARL": return ItemTypes.ENDER_PEARL.get();
            case "FEATHER": return ItemTypes.FEATHER.get();
            case "IRON_CHESTPLATE": return ItemTypes.IRON_CHESTPLATE.get();
            case "TRIPWIRE_HOOK": return ItemTypes.TRIPWIRE_HOOK.get();
            case "LAVA_BUCKET": return ItemTypes.LAVA_BUCKET.get();
            case "ARROW": return ItemTypes.ARROW.get();
            default: return ItemTypes.STONE.get();
        }
    }
}
