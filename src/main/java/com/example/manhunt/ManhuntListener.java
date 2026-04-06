package com.example.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Random;

public class ManhuntListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public ManhuntListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getRunnerUUID() != null) {
            if (plugin.isHunter(player)) {
                plugin.giveCompass(player);
                plugin.updateAllCompassTargets();
            } else if (player.getUniqueId().equals(plugin.getRunnerUUID())) {
                plugin.cancelDisconnectCountdown();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getRunnerUUID() != null && player.getUniqueId().equals(plugin.getRunnerUUID())) {
            plugin.startDisconnectCountdown();
        }
    }

    @EventHandler
    public void onCompassInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isHunter(player)) return;
        if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
            plugin.updateAllCompassTargets();

            // Eğer aktif bir disconnect sayacı varsa, onu göster (mesafe mesajını bastır)
            if (plugin.isDisconnectCountdown()) {
                // Action bar zaten disconnect task tarafından sürekli güncelleniyor.
                // Burada ekstra bir şey yapmaya gerek yok.
                event.setCancelled(true);
                return;
            }

            Player runner = plugin.getRunner();
            if (runner != null && plugin.isTrackingActive()) {
                double distance = player.getLocation().distance(runner.getLocation());
                plugin.getMessageManager().sendActionBar(player, "compass_updated", (int) distance);
            } else if (!plugin.isTrackingActive() && plugin.getGameStartTime() > 0) {
                long elapsed = (System.currentTimeMillis() - plugin.getGameStartTime()) / 1000;
                long remaining = 600 - elapsed;
                if (remaining > 0) {
                    plugin.getMessageManager().sendActionBar(player, "tracking_inactive", remaining);
                } else if (runner != null) {
                    double distance = player.getLocation().distance(runner.getLocation());
                    plugin.getMessageManager().sendActionBar(player, "compass_updated", (int) distance);
                } else {
                    plugin.getMessageManager().sendMessage(player, "runner_not_found");
                }
            } else {
                plugin.getMessageManager().sendMessage(player, "runner_not_found");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (plugin.getRunner() != null && player.getUniqueId().equals(plugin.getRunnerUUID())) {
            Location from = event.getFrom();
            plugin.setLastPortalLocation(from.clone());
            plugin.updateAllCompassTargets();
        }
    }

    @EventHandler
    public void onHunterDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!plugin.isHunter(player)) return;

        event.setKeepInventory(true);
        event.getDrops().clear();

        PlayerInventory inv = player.getInventory();
        ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece != null && piece.getType().getMaxDurability() > 0) {
                int maxDur = piece.getType().getMaxDurability();
                int currentDamage = piece.getDurability();
                int reduction = (int) (maxDur * 0.15);
                int newDamage = currentDamage + reduction;
                if (newDamage >= maxDur) {
                    newDamage = maxDur - 1;
                }
                piece.setDurability((short) newDamage);
                armor[i] = piece;
            }
        }
        inv.setArmorContents(armor);
    }

    @EventHandler
    public void onHunterRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isHunter(player)) return;

        Player runner = plugin.getRunner();
        if (runner == null) return;

        Location runnerLoc = runner.getLocation();
        World world = runnerLoc.getWorld();
        if (world == null) return;

        Location safeLocation = findSafeLocationNear(world, runnerLoc, 50, 100);
        if (safeLocation != null) {
            event.setRespawnLocation(safeLocation);
        }
    }

    private Location findSafeLocationNear(World world, Location center, int minDist, int maxDist) {
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minDist + random.nextDouble() * (maxDist - minDist);
            int x = center.getBlockX() + (int) (Math.cos(angle) * distance);
            int z = center.getBlockZ() + (int) (Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z);
            Location candidate = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (world.getBlockAt(x, y, z).getType().isSolid() &&
                world.getBlockAt(x, y + 1, z).isPassable() &&
                world.getBlockAt(x, y + 2, z).isPassable()) {
                return candidate;
            }
        }
        return null;
    }

    @EventHandler
    public void onMaceAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player runner = plugin.getRunner();
        if (runner == null || !victim.equals(runner)) return;

        if (!plugin.isHunter(damager)) return;

        ItemStack weapon = damager.getInventory().getItemInMainHand();
        if (weapon.getType() != Material.MACE) return;

        if (damager.isOnGround()) return;

        boolean hasElytra = runner.getInventory().getChestplate() != null &&
                            runner.getInventory().getChestplate().getType() == Material.ELYTRA;
        if (hasElytra && runner.isGliding()) {
            double newDamage = event.getDamage() * 0.8;
            event.setDamage(newDamage);
            plugin.getMessageManager().sendMessage(runner, "elytra_mace_reduce");
            plugin.getMessageManager().sendMessage(damager, "mace_damage_reduced");
        }
    }

    @EventHandler
    public void onPortalBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getRunner() == null) return;
        if (!player.getUniqueId().equals(plugin.getRunnerUUID())) return;

        Material type = event.getBlock().getType();
        if (type == Material.NETHER_PORTAL || type == Material.END_PORTAL) {
            for (Player hunter : player.getWorld().getPlayers()) {
                if (plugin.isHunter(hunter)) {
                    plugin.getMessageManager().sendMessage(hunter, "portal_link_lost");
                }
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getRunnerUUID() != null) {
            event.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        Player killer = dragon.getKiller();
        if (killer == null) return;

        Player runner = plugin.getRunner();
        if (runner == null) return;

        if (killer.getUniqueId().equals(runner.getUniqueId())) {
            plugin.endGame(false);
        }
    }

    @EventHandler
    public void onRunnerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (plugin.getRunnerUUID() == null) return;
        if (!victim.getUniqueId().equals(plugin.getRunnerUUID())) return;

        plugin.endGame(true);
    }
}