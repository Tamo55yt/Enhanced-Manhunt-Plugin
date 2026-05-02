package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SetupMenu extends BaseMenu {

    public static final java.util.Map<java.util.UUID, String> awaitingChatInput = new java.util.concurrent.ConcurrentHashMap<>();

    public SetupMenu(@NotNull Main plugin) {
        super(plugin, "&8Sayaç / Hedef Ayarı", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();

        int target = (int) plugin.getConfig().getDouble("tournament.target-score", 10.0);
        int mins = plugin.getConfig().getInt("timer.countdown-minutes", 60);

        inventory.setItem(11, createItem(getMaterial("OAK_SIGN", "SIGN"), "&eGeri Sayım Süresi", "&7Mevcut: &a" + mins + " dk", "&7Tıklayıp chat'e yeni süreyi yazın."));
        inventory.setItem(15, createItem(getMaterial("OAK_SIGN", "SIGN"), "&bHedef Puan", "&7Mevcut: &a" + target + " puan", "&7Tıklayıp chat'e yeni puanı yazın."));

        inventory.setItem(21, createGlass(true, "&aGeri Dön"));
        inventory.setItem(23, createGlass(false, "&cMenüyü Kapat"));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name == null) return;

        if (name.contains("Geri Sayım Süresi")) {
            awaitingChatInput.put(player.getUniqueId(), "countdown");
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Lütfen chat'e yeni geri sayım süresini (dakika cinsinden) yazın:");
        } else if (name.contains("Hedef Puan")) {
            awaitingChatInput.put(player.getUniqueId(), "target");
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Lütfen chat'e yeni hedef puanı yazın:");
        } else if (name.contains("Geri Dön")) {
            new MainMenu(plugin).open(player);
        } else if (name.contains("Menüyü Kapat")) {
            player.closeInventory();
        }
    }

    public void open(Player player) {
        createInventory();
        player.openInventory(getInventory());
    }
}
