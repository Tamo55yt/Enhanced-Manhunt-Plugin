package com.example.manhunt;

import com.example.manhunt.api.KitProvider;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.bukkit.impl.BukkitPlayer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class KitManager implements KitProvider {

    private final Main plugin;

    private static Method getAttributeMethod;
    private static Object healthAttribute;
    private static Method setBaseValueMethod;
    private static boolean attributeApiAvailable = false;

    static {
        try {
            Class<?> attributeClass = Class.forName("org.bukkit.attribute.Attribute");
            healthAttribute = attributeClass.getField("GENERIC_MAX_HEALTH").get(null);
            getAttributeMethod = Player.class.getMethod("getAttribute", attributeClass);
            setBaseValueMethod = Class.forName("org.bukkit.attribute.AttributeInstance").getMethod("setBaseValue", double.class);
            attributeApiAvailable = true;
        } catch (Exception ignored) {
            // Legacy version (1.12 and below)
        }
    }

    public KitManager(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveKit(@NotNull MPlayer mPlayer, @NotNull String role) {
        if (!(mPlayer instanceof BukkitPlayer)) return;
        Player player = ((BukkitPlayer) mPlayer).getBukkitPlayer();
        
        FileConfiguration config = plugin.getConfig();
        String path = "kits." + role;

        config.set(path, null);

        ConfigurationSection itemsSection = config.createSection(path + ".items");
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item != null && item.getType() != Material.AIR) {
                itemsSection.set(String.valueOf(slot), item);
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

    private void saveArmorPiece(@NotNull ConfigurationSection section, @NotNull String slot, @Nullable ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            section.set(slot, item);
        }
    }

    @Override
    public void applyKit(@NotNull MPlayer mPlayer, @NotNull String role) {
        if (!(mPlayer instanceof BukkitPlayer)) return;
        Player player = ((BukkitPlayer) mPlayer).getBukkitPlayer();

        FileConfiguration config = plugin.getConfig();
        String path = "kits." + role;

        if (!config.isConfigurationSection(path)) {
            plugin.getMessageManager().sendMessage(player, "kit_not_found", role);
            return;
        }

        player.getInventory().clear();

        double health = config.getDouble(path + ".max-health", 20.0);
        setPlayerMaxHealth(player, health);

        ConfigurationSection itemsSec = config.getConfigurationSection(path + ".items");
        if (itemsSec != null) {
            for (String key : itemsSec.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ItemStack item = itemsSec.getItemStack(key);
                    if (item != null) {
                        player.getInventory().setItem(slot, item);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        ConfigurationSection armorSec = config.getConfigurationSection(path + ".armor");
        if (armorSec != null) {
            player.getInventory().setHelmet(armorSec.getItemStack("helmet"));
            player.getInventory().setChestplate(armorSec.getItemStack("chestplate"));
            player.getInventory().setLeggings(armorSec.getItemStack("leggings"));
            player.getInventory().setBoots(armorSec.getItemStack("boots"));
        }

        player.updateInventory();
        plugin.getMessageManager().sendMessage(player, "kit_applied", role);
    }

    @SuppressWarnings("deprecation")
    private void setPlayerMaxHealth(@NotNull Player player, double health) {
        try {
            if (attributeApiAvailable) {
                Object attributeInstance = getAttributeMethod.invoke(player, healthAttribute);
                if (attributeInstance != null) {
                    setBaseValueMethod.invoke(attributeInstance, health);
                }
            } else {
                player.setMaxHealth(health);
            }
            player.setHealth(Math.min(player.getMaxHealth(), health));
        } catch (Exception e) {
            try { player.setHealth(Math.min(player.getMaxHealth(), health)); } catch (Exception ignored) {}
        }
    }
}
