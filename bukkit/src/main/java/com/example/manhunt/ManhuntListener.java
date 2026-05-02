package com.example.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManhuntListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public ManhuntListener(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getGameManager().isGameRunning()) {
            if (plugin.getGameManager().isHunter(player)) {
                plugin.getCompassManager().giveCompass(player);
            } else if (plugin.getGameManager().isRunner(player)) {
                plugin.getGameManager().cancelDisconnectCountdown();
            }
        }
        plugin.updateTabList();
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isRunner(player)) {
            plugin.getGameManager().startDisconnectCountdown();
        }
        plugin.getCompassManager().cleanupPlayer(player.getUniqueId());
        com.example.manhunt.menu.SetupMenu.awaitingChatInput.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCompassInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;

        if (player.getGameMode() == GameMode.SPECTATOR && plugin.getGameManager().isHunter(player)) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                new com.example.manhunt.menu.SpectatorTeleportMenu(plugin).open(player);
            } else {
                Player runner = plugin.getGameManager().getBukkitFirstRunner();
                if (runner != null && runner.isOnline()) {
                    player.teleport(runner.getLocation());
                    plugin.getMessageManager().sendMessage(player, "spectator_teleport_runner");
                }
            }
            event.setCancelled(true);
            return;
        }

        if (event.getAction().name().contains("RIGHT_CLICK") && plugin.getGameManager().isHunter(player)) {
            plugin.getCompassManager().cycleCompassMode(player);
            event.setCancelled(true);
            return;
        }

        if (event.getAction().name().contains("LEFT_CLICK") && plugin.getGameManager().isHunter(player)) {
            org.bukkit.block.Block targetBlock = player.getTargetBlock((java.util.Set<Material>) null, 100);
            if (targetBlock != null && targetBlock.getType() != Material.AIR) {
                String locStr = targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ();
                String msg = ChatColor.GOLD + "[PING] " + ChatColor.YELLOW + player.getName() + ": " + ChatColor.WHITE + "Buraya bakın! (" + locStr + ")";
                
                // Visual Particle Pillar
                final Location pLoc = targetBlock.getLocation().add(0.5, 0, 0.5);
                for (int i = 0; i < 10; i++) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        for (int y = 0; y < 256; y += 2) {
                            pLoc.getWorld().spawnParticle(org.bukkit.Particle.FLAME, pLoc.getX(), y, pLoc.getZ(), 1, 0.1, 0.1, 0.1, 0.05);
                        }
                    }, i * 20L);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getGameManager().isHunter(p)) {
                        p.sendMessage(msg);
                        try { p.playSound(targetBlock.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {}
                    }
                }
            }
            event.setCancelled(true);
            return;
        }

        // Runner Smoke Bomb
        if (event.getAction().name().contains("RIGHT_CLICK") && plugin.getGameManager().isSmokeBombEnabled() && 
            plugin.getGameManager().isRunner(player) && 
            item.getType() == Material.GUNPOWDER) {
            
            long lastUse = plugin.getGameManager().getAbilityCooldowns().getOrDefault(player.getUniqueId(), 0L);
            long now = System.currentTimeMillis();
            if (now - lastUse < 600000L) { // 10 minutes
                long remaining = (600000L - (now - lastUse)) / 1000L;
                player.sendMessage(ChatColor.RED + "Sis bombası hazır değil! Kalan: " + remaining + "s");
                return;
            }

            plugin.getGameManager().getAbilityCooldowns().put(player.getUniqueId(), now);
            player.sendMessage(ChatColor.DARK_GRAY + "Sis bombası kullanıldı!");
            player.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, player.getLocation(), 100, 2, 2, 2, 0.1);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getGameManager().isHunter(p) && p.getLocation().distance(player.getLocation()) < 15) {
                    @SuppressWarnings("deprecation")
                    org.bukkit.potion.PotionEffectType blindness = org.bukkit.potion.PotionEffectType.getByName("BLINDNESS");
                    @SuppressWarnings("deprecation")
                    org.bukkit.potion.PotionEffectType slowness = org.bukkit.potion.PotionEffectType.getByName("SLOW");
                    
                    if (blindness != null) p.addPotionEffect(new org.bukkit.potion.PotionEffect(blindness, 60, 1));
                    if (slowness != null) p.addPotionEffect(new org.bukkit.potion.PotionEffect(slowness, 60, 2));
                    p.sendMessage(ChatColor.RED + "Runner sis bombası kullandı! Gözlerin yanıyor!");
                }
            }
            return;
        }

        if (plugin.getGameManager().isRunner(player)) {
            if (plugin.getCompassManager().getRunnerTrackTarget() == null) {
                plugin.getMessageManager().sendMessage(player, "runner_compass_usage");
            } else {
                Player target = Bukkit.getPlayer(plugin.getCompassManager().getRunnerTrackTarget());
                if (target != null && target.isOnline()) {
                    if (player.getWorld().equals(target.getWorld())) {
                        double dist = player.getLocation().distance(target.getLocation());
                        int yDiff = target.getLocation().getBlockY() - player.getLocation().getBlockY();
                        String yStr = yDiff > 0 ? "§a+" + yDiff : "§c" + yDiff;
                        player.sendMessage(ChatColor.AQUA + "Hedef: " + ChatColor.WHITE + target.getName() + " | " + (int)dist + "m | Y: " + yStr);
                    } else {
                        plugin.getMessageManager().sendMessage(player, "hunter_different_world");
                    }
                }
            }
            event.setCancelled(true);
            return;
        }

        if (plugin.getGameManager().isHunter(player)) {
            if (plugin.getGameManager().isDisconnectCountdown()) return;
            plugin.getCompassManager().updateAllCompassTargets();
            plugin.getMessageManager().sendMessage(player, "compass_pointing_runner");
            event.setCancelled(true);
        }
    }

    private static final Material[] VALID_DROPS;
    static {
        List<Material> temp = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.isItem() && m != Material.AIR) temp.add(m);
        }
        VALID_DROPS = temp.toArray(new Material[0]);
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        if (!plugin.getGameManager().isGameRunning()) return;

        // Increment leveling progress
        plugin.getLootPoolManager().incrementProgress();

        // Modular LootPool check
        List<String> customLoot = plugin.getLootPoolManager().getLoot(event.getBlock().getType().name());
        if (customLoot != null) {
            event.setDropItems(false);
            for (String matStr : customLoot) {
                try {
                    Material mat = Material.valueOf(matStr);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(mat));
                } catch (Exception ignored) {}
            }
        }

        if (plugin.getScenarioManager().getSelectedScenario().equals("RANDOM_DROPS")) {
            event.setDropItems(false);
            Material randomMat = VALID_DROPS[random.nextInt(VALID_DROPS.length)];
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(randomMat));
        }
    }

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!plugin.getGameManager().isGameRunning()) return;

        List<String> customLoot = plugin.getLootPoolManager().getLoot(event.getEntityType().name());
        if (customLoot != null) {
            event.getDrops().clear();
            for (String matStr : customLoot) {
                try {
                    Material mat = Material.valueOf(matStr);
                    event.getDrops().add(new ItemStack(mat));
                } catch (Exception ignored) {}
            }
        }

        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon dragon = (EnderDragon) event.getEntity();
        Player killer = dragon.getKiller();
        if (killer != null && plugin.getGameManager().isRunner(killer)) {
            Location loc = dragon.getLocation();
            plugin.getGameManager().endGame(new com.example.manhunt.api.MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()), false);
        }
    }

    @EventHandler
    public void onAdvancement(@NotNull org.bukkit.event.player.PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        org.bukkit.advancement.Advancement adv = event.getAdvancement();
        String key = adv.getKey().getKey();
        if (key.contains("recipes/")) return;

        String advName = key.substring(key.lastIndexOf('/') + 1).replace('_', ' ');
        if (!advName.isEmpty()) advName = advName.substring(0, 1).toUpperCase() + advName.substring(1);

        boolean isEarnerHunter = plugin.getGameManager().isHunter(player);
        boolean isAnnounceEnabled = plugin.getGameManager().isAdvancementAnnouncementsEnabled();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (plugin.getGameManager().isHunter(p)) {
                // Hunters always see other hunters' progress
                if (isEarnerHunter) {
                    p.sendTitle(ChatColor.GOLD + player.getName(), ChatColor.WHITE + "[" + advName + "] Başarısını Kazandı!", 10, 40, 10);
                    plugin.getMessageManager().sendMessage(p, "advancement_hunter", player.getName(), advName);
                } 
                // Hunters see runner progress only if announcements are toggled ON in menu
                else if (isAnnounceEnabled) {
                    p.sendTitle(ChatColor.RED + player.getName(), ChatColor.YELLOW + "Runner [" + advName + "] Aldı!", 10, 40, 10);
                }
            }
        }
    }

    @EventHandler
    public void onHunterMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (plugin.getGameManager().isGracePeriod() && plugin.getGameManager().isHunter(event.getPlayer())) {
            if (!plugin.getConfig().getBoolean("grace-period.allow-movement", false)) {
                Location from = event.getFrom();
                Location to = event.getTo();
                if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                    event.setTo(from.setDirection(to.getDirection()));
                }
            }
        }
    }

    @EventHandler
    public void onHunterDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (plugin.getGameManager().isGracePeriod() && event.getDamager() instanceof Player) {
            Player p = (Player) event.getDamager();
            if (plugin.getGameManager().isHunter(p)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPortal(@NotNull PlayerPortalEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isRunner(player)) {
            Location from = event.getFrom();
            if (from.getWorld() != null) {
                plugin.getCompassManager().setLastPortalLocation(from.clone());
                
                String targetWorld = event.getTo() != null && event.getTo().getWorld() != null ? 
                    (event.getTo().getWorld().getEnvironment() == World.Environment.NETHER ? "Nether" : "Dünya") : "Bilinmeyen";

                // Fancy Title for everyone
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(ChatColor.RED + player.getName(), ChatColor.YELLOW + targetWorld + "'e Geçiş Yaptı!", 10, 40, 10);
                }

                // Coordinates for hunters
                String coords = ChatColor.GOLD + "[PORTAL] " + ChatColor.YELLOW + "Runner " + targetWorld + "'e girdi! Koordinatlar: " + 
                                ChatColor.WHITE + from.getBlockX() + ", " + from.getBlockY() + ", " + from.getBlockZ();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getGameManager().isHunter(p)) p.sendMessage(coords);
                }

                SchedulerUtils.runTaskLater(plugin, () -> plugin.getCompassManager().updateAllCompassTargets(), 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        plugin.getGameManager().handleDeath(event);
    }

    @EventHandler
    public void onHunterRespawn(@NotNull PlayerRespawnEvent event) {
        plugin.getGameManager().handleRespawn(event);
    }

    @EventHandler
    public void onEnderDragonDeath(@NotNull EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        EnderDragon dragon = (EnderDragon) event.getEntity();
        Player killer = dragon.getKiller();
        if (killer != null && plugin.getGameManager().isRunner(killer)) {
            Location loc = dragon.getLocation();
            plugin.getGameManager().endGame(new com.example.manhunt.api.MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()), false);
        }
    }

    @EventHandler
    public void onChat(@NotNull org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (com.example.manhunt.menu.SetupMenu.awaitingChatInput.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String type = com.example.manhunt.menu.SetupMenu.awaitingChatInput.remove(player.getUniqueId());
            String msg = event.getMessage();
            
            try {
                if (type.equals("countdown")) {
                    int mins = Integer.parseInt(msg);
                    plugin.getConfig().set("timer.countdown-minutes", mins);
                    player.sendMessage(ChatColor.GREEN + "Geri sayım süresi " + mins + " dakika olarak ayarlandı.");
                }
                plugin.saveConfig();
                com.example.manhunt.SchedulerUtils.runTask(plugin, () -> new com.example.manhunt.menu.SetupMenu(plugin).open(player));
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Geçersiz sayı! İşlem iptal edildi.");
            }
        }
    }

    @EventHandler
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        if (plugin.getGameManager().isGameRunning()) {
            World world = event.getWorld();
            try {
                // Modern API (1.13+)
                try {
                    Class<?> gameRuleClass = Class.forName("org.bukkit.GameRule");
                    Object announceAdvancements = gameRuleClass.getField("ANNOUNCE_ADVANCEMENTS").get(null);
                    java.lang.reflect.Method setGameRule = World.class.getMethod("setGameRule", gameRuleClass, Object.class);
                    setGameRule.invoke(world, announceAdvancements, false);
                } catch (Exception e) {
                    // Legacy API (1.12-)
                    java.lang.reflect.Method setGameRuleValue = World.class.getMethod("setGameRuleValue", String.class, String.class);
                    setGameRuleValue.invoke(world, "announceAdvancements", "false");
                }
            } catch (Exception ignored) {}
        }
    }
}
