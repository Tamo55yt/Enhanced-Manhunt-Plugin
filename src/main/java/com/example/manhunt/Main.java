package com.example.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class Main extends JavaPlugin {

    private static Main instance;
    private UUID runnerUUID;
    private Location lastPortalLocation;
    private int compassTaskId;
    private KitManager kitManager;
    private MessageManager messageManager;

    private boolean isTrackingActive = false;
    private BukkitTask trackingCountdownTask;
    private long gameStartTime;

    // Runner disconnect sistemi
    private BukkitTask disconnectTask;
    private boolean isDisconnectCountdown = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        kitManager = new KitManager(this);
        messageManager = new MessageManager(this);

        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        Bukkit.getPluginManager().registerEvents(new ManhuntListener(this), this);

        compassTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllCompassTargets();
            }
        }.runTaskTimer(this, 0L, 20L).getTaskId();

        getLogger().info("Manhunt Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (trackingCountdownTask != null && !trackingCountdownTask.isCancelled()) {
            trackingCountdownTask.cancel();
        }
        if (disconnectTask != null && !disconnectTask.isCancelled()) {
            disconnectTask.cancel();
        }
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Manhunt Plugin disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public UUID getRunnerUUID() {
        return runnerUUID;
    }

    public void setRunnerUUID(UUID runnerUUID) {
        this.runnerUUID = runnerUUID;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isHunter(online)) {
                giveCompass(online);
            }
        }
        updateAllCompassTargets();
    }

    public Location getLastPortalLocation() {
        return lastPortalLocation;
    }

    public void setLastPortalLocation(Location lastPortalLocation) {
        this.lastPortalLocation = lastPortalLocation;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public boolean isHunter(Player player) {
        return runnerUUID != null && !player.getUniqueId().equals(runnerUUID);
    }

    public Player getRunner() {
        if (runnerUUID == null) return null;
        return Bukkit.getPlayer(runnerUUID);
    }

    public boolean isTrackingActive() {
        return isTrackingActive;
    }

    public long getGameStartTime() {
        return gameStartTime;
    }

    public boolean isDisconnectCountdown() {
        return isDisconnectCountdown;
    }

    public void updateAllCompassTargets() {
        Player runner = getRunner();
        Location target;

        for (Player hunter : Bukkit.getOnlinePlayers()) {
            if (!isHunter(hunter)) continue;

            if (isTrackingActive && runner != null && hunter.getWorld().equals(runner.getWorld())) {
                target = runner.getLocation();
            } else if (isTrackingActive && runner != null && lastPortalLocation != null
                    && lastPortalLocation.getWorld().equals(hunter.getWorld())) {
                target = lastPortalLocation;
            } else {
                target = hunter.getWorld().getSpawnLocation();
            }
            hunter.setCompassTarget(target);
        }
    }

    public void giveCompass(Player hunter) {
        if (!hunter.getInventory().contains(org.bukkit.Material.COMPASS)) {
            hunter.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COMPASS));
        }
    }

    public void setAnnounceAdvancementsGlobal(boolean enabled) {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, enabled);
        }
    }

    public void startGame() {
        startGame(true);
    }

    public void startGame(boolean applyKits) {
        if (runnerUUID == null) {
            Bukkit.broadcastMessage("§cCannot start game: No runner selected!");
            return;
        }

        gameStartTime = System.currentTimeMillis();
        setAnnounceAdvancementsGlobal(false);

        for (Player online : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                online.sendMessage(" ");
            }
            online.sendMessage("§a§l=== MANHUNT STARTED ===§r");
        }

        if (applyKits) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.getInventory().clear();
                online.getInventory().setArmorContents(null);
                if (online.getUniqueId().equals(runnerUUID)) {
                    kitManager.applyKit(online, "runner");
                } else {
                    kitManager.applyKit(online, "hunter");
                    giveCompass(online);
                }
            }
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (isHunter(online)) {
                    giveCompass(online);
                }
            }
        }

        startTrackingDelay();
        messageManager.broadcast("game_started", applyKits ? messageManager.getMessage("game_started_kits") : messageManager.getMessage("game_started_no_kits"));
    }

    private void startTrackingDelay() {
        if (trackingCountdownTask != null && !trackingCountdownTask.isCancelled()) {
            trackingCountdownTask.cancel();
        }
        isTrackingActive = false;
        trackingCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                isTrackingActive = true;
                messageManager.broadcast("tracking_active");
                updateAllCompassTargets();
            }
        }.runTaskLater(this, 20L * 600);
    }

    /**
     * Runner oyundan ayrıldığında çağrılır.
     */
    public void startDisconnectCountdown() {
        if (disconnectTask != null && !disconnectTask.isCancelled()) {
            disconnectTask.cancel();
        }
        isDisconnectCountdown = true;
        disconnectTask = new BukkitRunnable() {
            int secondsLeft = 60;
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    messageManager.broadcast("runner_timeout");
                    endGame(true);
                    cancel();
                    isDisconnectCountdown = false;
                    disconnectTask = null;
                } else {
                    // Her saniye tüm avcılara action bar mesajı gönder
                    for (Player hunter : Bukkit.getOnlinePlayers()) {
                        if (isHunter(hunter)) {
                            messageManager.sendActionBar(hunter, "logout_countdown_actionbar", secondsLeft);
                        }
                    }
                    secondsLeft--;
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    /**
     * Runner geri döndüğünde sayacı iptal eder.
     */
    public void cancelDisconnectCountdown() {
        if (disconnectTask != null && !disconnectTask.isCancelled()) {
            disconnectTask.cancel();
            disconnectTask = null;
        }
        if (isDisconnectCountdown) {
            isDisconnectCountdown = false;
            messageManager.broadcast("runner_returned");
        }
    }

    /**
     * Oyunu bitirir.
     * @param hunterWin true ise hunter kazanır, false ise runner kazanır
     */
    public void endGame(boolean hunterWin) {
        setAnnounceAdvancementsGlobal(true);
        if (trackingCountdownTask != null && !trackingCountdownTask.isCancelled()) {
            trackingCountdownTask.cancel();
        }
        if (disconnectTask != null && !disconnectTask.isCancelled()) {
            disconnectTask.cancel();
            disconnectTask = null;
        }
        isTrackingActive = false;
        isDisconnectCountdown = false;

        String titleKey = hunterWin ? "hunter_winner_title" : "runner_winner_title";
        for (Player online : Bukkit.getOnlinePlayers()) {
            messageManager.sendTitle(online, titleKey, 10, 100, 20);
        }
    }

    public void endGame() {
        endGame(true);
    }
}