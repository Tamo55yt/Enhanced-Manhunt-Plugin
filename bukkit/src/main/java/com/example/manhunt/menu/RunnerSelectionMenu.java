package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RunnerSelectionMenu extends BaseMenu {

    public RunnerSelectionMenu(@NotNull Main plugin) {
        super(plugin, "&8Runner Seçiniz", 54);
    }

    @Override
    protected void createInventory() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int calcSize = ((onlinePlayers.size() / 9) + 1) * 9;
        if (calcSize > 54) calcSize = 54;
        
        this.inventory = Bukkit.createInventory(this, calcSize, title);

        for (int i = 0; i < onlinePlayers.size() && i < 54; i++) {
            Player p = onlinePlayers.get(i);
            inventory.setItem(i, getPlayerHead(p));
        }
        
        // Add settings/utility buttons at the end if there's space
        if (inventory.getSize() >= 54) {
            inventory.setItem(53, createItem(Material.ANVIL, "&eAyarları Düzenle", "&7Puan ve sayaç ayarları."));
        }
    }

    @SuppressWarnings("deprecation")
    private ItemStack getPlayerHead(Player player) {
        Material type = getMaterial("PLAYER_HEAD", "SKULL_ITEM");
        ItemStack item = new ItemStack(type, 1, (short) (type.name().equals("SKULL_ITEM") ? 3 : 0));
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta) {
            SkullMeta skull = (SkullMeta) meta;
            try {
                skull.setOwningPlayer(player);
            } catch (NoSuchMethodError | Exception e) {
                skull.setOwner(player.getName());
            }
            skull.setDisplayName(ChatColor.AQUA + player.getName());
            List<String> lore = new ArrayList<>();
            boolean isRunner = plugin.getGameManager().getRunners().contains(player.getUniqueId());
            lore.add(isRunner ? ChatColor.GREEN + "Şu an Runner" : ChatColor.RED + "Runner Değil");
            lore.add("");
            lore.add(ChatColor.GRAY + "Durumu değiştirmek için tıklayın.");
            skull.setLore(lore);
            item.setItemMeta(skull);
        }
        return item;
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player admin = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.contains("Ayarları Düzenle")) {
            new SetupMenu(plugin).open(admin);
            return;
        }

        Player target = Bukkit.getPlayer(name);
        if (target != null) {
            if (plugin.getGameManager().getRunners().contains(target.getUniqueId())) {
                plugin.getGameManager().removeRunner(target.getUniqueId());
                plugin.getMessageManager().sendMessage(admin, "runner_removed", target.getName());
            } else {
                plugin.getGameManager().addRunner(target.getUniqueId());
                plugin.getMessageManager().sendMessage(admin, "runner_added", target.getName());
            }
            open(admin);
        }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
