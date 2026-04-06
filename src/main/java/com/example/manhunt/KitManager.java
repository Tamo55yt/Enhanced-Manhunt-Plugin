package com.example.manhunt;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class KitManager {

    private final Main plugin;

    public KitManager(Main plugin) {
        this.plugin = plugin;
    }

    public void saveKit(Player player, String role) {
        FileConfiguration config = plugin.getConfig();
        String path = "kits." + role;

        ConfigurationSection itemsSection = config.createSection(path + ".items");
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item != null && item.getType() != Material.AIR) {
                itemsSection.set(String.valueOf(slot), serializeItem(item));
            }
        }

        ConfigurationSection armorSection = config.createSection(path + ".armor");
        PlayerInventory inv = player.getInventory();
        saveArmorPiece(armorSection, "helmet", inv.getHelmet());
        saveArmorPiece(armorSection, "chestplate", inv.getChestplate());
        saveArmorPiece(armorSection, "leggings", inv.getLeggings());
        saveArmorPiece(armorSection, "boots", inv.getBoots());

        plugin.saveConfig();
        plugin.getMessageManager().sendMessage(player, "kit_saved", role);
    }

    private void saveArmorPiece(ConfigurationSection parent, String slotName, ItemStack armor) {
        if (armor != null && armor.getType() != Material.AIR) {
            parent.set(slotName, serializeItem(armor));
        }
    }

    private Map<String, Object> serializeItem(ItemStack item) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", item.getType().name());
        map.put("amount", item.getAmount());

        if (!item.getEnchantments().isEmpty()) {
            List<Map<String, Object>> enchants = new ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                Map<String, Object> ench = new HashMap<>();
                ench.put("type", entry.getKey().getKey().getKey());
                ench.put("level", entry.getValue());
                enchants.add(ench);
            }
            map.put("enchantments", enchants);
        }
        return map;
    }

    public void applyKit(Player player, String role) {
        FileConfiguration config = plugin.getConfig();
        String path = "kits." + role;

        if (!config.isConfigurationSection(path)) {
            plugin.getMessageManager().sendMessage(player, "kit_not_found", role);
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ConfigurationSection itemsSec = config.getConfigurationSection(path + ".items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
                    if (itemSec != null) {
                        ItemStack item = deserializeItem(itemSec);
                        if (item != null) {
                            player.getInventory().setItem(slot, item);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        ConfigurationSection armorSec = config.getConfigurationSection(path + ".armor");
        if (armorSec != null) {
            player.getInventory().setHelmet(deserializeItem(armorSec.getConfigurationSection("helmet")));
            player.getInventory().setChestplate(deserializeItem(armorSec.getConfigurationSection("chestplate")));
            player.getInventory().setLeggings(deserializeItem(armorSec.getConfigurationSection("leggings")));
            player.getInventory().setBoots(deserializeItem(armorSec.getConfigurationSection("boots")));
        }

        player.updateInventory();
        plugin.getMessageManager().sendMessage(player, "kit_applied", role);
    }

    private ItemStack deserializeItem(ConfigurationSection sec) {
        if (sec == null) return null;

        String typeName = sec.getString("type");
        if (typeName == null) return null;

        Material material = Material.getMaterial(typeName);
        if (material == null || material == Material.AIR) return null;

        int amount = sec.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);

        Object enchantsObj = sec.get("enchantments");
        if (enchantsObj instanceof List<?> enchantList) {
            for (Object elem : enchantList) {
                if (elem instanceof Map<?, ?> enchMap) {
                    Object typeObj = enchMap.get("type");
                    Object levelObj = enchMap.get("level");
                    if (typeObj instanceof String enchName && levelObj instanceof Number level) {
                        NamespacedKey key = NamespacedKey.minecraft(enchName.toLowerCase());
                        Enchantment enchant = Enchantment.getByKey(key);
                        if (enchant != null) {
                            item.addUnsafeEnchantment(enchant, level.intValue());
                        }
                    }
                }
            }
        }
        return item;
    }
}