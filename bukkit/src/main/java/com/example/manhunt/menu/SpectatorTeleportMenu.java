package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpectatorTeleportMenu extends BaseMenu {

    public SpectatorTeleportMenu(@NotNull Main plugin) {
        super(plugin, "&8Oyuncu Işınlanma", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        
        int slot = 0;
        
        // Add Runners
        for (UUID uuid : plugin.getGameManager().getRunners()) {
            Player runner = Bukkit.getPlayer(uuid);
            if (runner != null && runner.isOnline()) {
                inventory.setItem(slot++, createPlayerHead(runner, "&b&lRUNNER: &f" + runner.getName(), "&7Tıklayarak ışınlan."));
                if (slot >= 27) break;
            }
        }

        // Add Alive Hunters
        if (slot < 27) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isHunter(p) && !plugin.getGameManager().getDeadHunters().contains(p.getUniqueId())) {
                    inventory.setItem(slot++, createPlayerHead(p, "&6&lHUNTER: &f" + p.getName(), "&7Tıklayarak ışınlan."));
                    if (slot >= 27) break;
                }
            }
        }
    }

    private ItemStack createPlayerHead(Player player, String name, String... lore) {
        ItemStack item = new ItemStack(getMaterial("PLAYER_HEAD"));
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) coloredLore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player spectator = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name.contains(": ")) {
            String targetName = name.split(": ")[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target != null && target.isOnline()) {
                spectator.teleport(target.getLocation());
                spectator.sendMessage(org.bukkit.ChatColor.GREEN + target.getName() + " yanına ışınlandın.");
                spectator.closeInventory();
            }
        }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
