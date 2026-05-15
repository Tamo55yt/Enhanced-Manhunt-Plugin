package com.example.manhunt.menu;

import com.example.manhunt.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScenarioMenu extends BaseMenu {

    public ScenarioMenu(@NotNull Main plugin) {
        super(plugin, "&8Senaryo Seçimi", 27);
    }

    @Override
    protected void createInventory() {
        super.createInventory();
        inventory.setItem(10, createItem(getMaterial("SHORT_GRASS", "GRASS"), "&fNormal Manhunt", "&7Standart oyun modu."));
        inventory.setItem(12, createItem(Material.LAVA_BUCKET, "&cLav Yükseliyor", "&7Her 2 dakikada bir lav seviyesi artar."));
        inventory.setItem(13, createItem(Material.EXPERIENCE_BOTTLE, "&bSeviyeli Eşya", "&7Süreç ilerledikçe eşya kalitesi artar."));
        inventory.setItem(14, createItem(Material.CHEST, "&6Rastgele Eşya", "&7Bloklardan rastgele eşya düşer."));
        inventory.setItem(15, createItem(Material.CRAFTING_TABLE, "&eRastgele Üretim", "&7Ürettiğiniz eşya rastgele bir eşyaya dönüşür."));
        inventory.setItem(16, createItem(Material.ENDER_PEARL, "&dYer Değiştirme", "&7Her 10 dakikada bir yerler değişir."));
        inventory.setItem(17, createItem(Material.FEATHER, "&fYerçekimi Değişimi", "&7Her 5 dakikada bir yerçekimi seviyesi değişir."));
        inventory.setItem(22, createGlass(true, "&aGeri Dön"));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (name.contains("Geri Dön")) {
            new MainMenu(plugin).open(player);
            return;
        }

        if (item.getType().name().contains("GRASS")) plugin.getScenarioManager().setSelectedScenario("NORMAL");
        else if (item.getType() == Material.LAVA_BUCKET) plugin.getScenarioManager().setSelectedScenario("LAVA");
        else if (item.getType() == Material.EXPERIENCE_BOTTLE) plugin.getScenarioManager().setSelectedScenario("LOOTLEVELING");
        else if (item.getType() == Material.CHEST) plugin.getScenarioManager().setSelectedScenario("LOOTPOOL");
        else if (item.getType() == Material.ENDER_PEARL) plugin.getScenarioManager().setSelectedScenario("SWAP");
        else if (item.getType() == Material.FEATHER) plugin.getScenarioManager().setSelectedScenario("GRAVITY_SHIFT");
        else if (item.getType() == Material.CRAFTING_TABLE) plugin.getScenarioManager().setSelectedScenario("RANDOM_CRAFT");

        player.sendMessage(ChatColor.GREEN + "Senaryo Seçildi: " + item.getItemMeta().getDisplayName());
        player.closeInventory();
    }

    public void open(Player player) {
        player.openInventory(getInventory());
    }
}
