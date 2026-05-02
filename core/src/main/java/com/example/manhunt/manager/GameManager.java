package com.example.manhunt.manager;

import com.example.manhunt.api.*;
import com.example.manhunt.message.MessageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GameManager {

    private final MPlatform platform;
    private final MessageManager messageManager;
    private final KitProvider kitProvider;

    private final Set<UUID> runners = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean isGameRunning = new AtomicBoolean(false);
    private final AtomicBoolean isHardcore = new AtomicBoolean(false);
    private final AtomicBoolean isFFAMode = new AtomicBoolean(false);
    private final AtomicBoolean isGracePeriod = new AtomicBoolean(false);
    private final AtomicBoolean isDisconnectCountdown = new AtomicBoolean(false);
    private final AtomicBoolean advancementAnnouncements = new AtomicBoolean(false);
    private final AtomicLong gameStartTime = new AtomicLong(0);

    private final AtomicBoolean isSmokeBombEnabled = new AtomicBoolean(false);
    private final AtomicInteger borderType = new AtomicInteger(0); // 0: None, 1: Fixed, 2: Dynamic
    private final AtomicInteger borderSize = new AtomicInteger(5000);
    private final Map<UUID, Long> abilityCooldowns = new ConcurrentHashMap<>();

    private final Set<UUID> deadHunters = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private volatile MScheduler.Task graceTask;
    private volatile MScheduler.Task disconnectTask;
    private volatile MScheduler.Task cleanupTask;
    private volatile MScheduler.Task countdownTask;
    private volatile MScheduler.Task borderTask;

    public GameManager(MPlatform platform, MessageManager messageManager, KitProvider kitProvider) {
        this.platform = platform;
        this.messageManager = messageManager;
        this.kitProvider = kitProvider;
        startCleanupTask();
    }

    private void startCleanupTask() {
        cancelTask(cleanupTask);
        cleanupTask = platform.getScheduler().runTaskTimer(() -> {
            if (!platform.getConfigBoolean("item-cleanup.enabled", true)) return;
            // Cleanup logic implementation here
        }, 600L, 600L);
    }

    private void startBorderTask() {
        cancelTask(borderTask);
        if (borderType.get() == 0) {
            for (MWorld world : platform.getWorlds()) platform.resetWorldBorder(world.getName());
            return;
        }
        
        borderTask = platform.getScheduler().runTaskTimer(() -> {
            if (!isGameRunning.get()) return;
            
            MPlayer runner = getFirstRunner();
            int currentType = borderType.get();
            int size = borderSize.get();

            if (currentType == 1) { // Fixed
                for (MWorld world : platform.getWorlds()) {
                    platform.setWorldBorder(world.getName(), 0, 0, size);
                }
            } else if (currentType == 2 && runner != null && runner.isOnline()) { // Dynamic
                MLocation loc = runner.getLocation();
                platform.setWorldBorder(loc.getWorldName(), loc.getX(), loc.getZ(), 500); 
            }
        }, 100L, 100L);
    }

    public void startDisconnectCountdown() {
        if (disconnectTask != null || !isGameRunning.get()) return;
        isDisconnectCountdown.set(true);
        int duration = platform.getConfigInt("disconnect-timeout", 60);
        messageManager.broadcast("runner_disconnected_countdown", duration);
        
        final AtomicInteger remaining = new AtomicInteger(duration);
        disconnectTask = platform.getScheduler().runTaskTimer(() -> {
            if (remaining.get() <= 0) {
                endGame(null, true); // Hunters win
                cancelDisconnectCountdown();
            } else {
                int current = remaining.get();
                if (current <= 10 || current % 30 == 0) {
                    messageManager.broadcast("runner_disconnected_countdown", current);
                }
                remaining.decrementAndGet();
            }
        }, 0L, 20L);
    }

    public void cancelDisconnectCountdown() {
        cancelTask(disconnectTask);
        disconnectTask = null;
        if (isDisconnectCountdown.compareAndSet(true, false)) {
            messageManager.broadcast("runner_reconnected");
        }
    }

    public void startCountdown() {
        if (isGameRunning.get() || countdownTask != null) return;
        
        final AtomicInteger countdown = new AtomicInteger(3);
        countdownTask = platform.getScheduler().runTaskTimer(() -> {
            int current = countdown.get();
            if (current > 0) {
                messageManager.broadcast("countdown_" + current);
                for (MPlayer p : platform.getOnlinePlayers()) {
                    p.sendTitle("§6" + current, "", 5, 20, 5);
                }
                countdown.decrementAndGet();
            } else {
                messageManager.broadcast("countdown_start");
                for (MPlayer p : platform.getOnlinePlayers()) {
                    p.sendTitle("§a" + messageManager.getRawMessage(p, "countdown_start_title"), "", 5, 30, 10);
                }
                startGame();
                cancelTask(countdownTask);
                countdownTask = null;
            }
        }, 0L, 20L);
    }

    public void startGame() {
        if (runners.isEmpty() && !isFFAMode.get()) return;
        isGameRunning.set(true);
        gameStartTime.set(System.currentTimeMillis());
        stopTasks();
        deadHunters.clear();
        abilityCooldowns.clear();
        startBorderTask();
        
        for (MPlayer online : platform.getOnlinePlayers()) {
            online.setGameMode("SURVIVAL");
            online.clearInventory();
            
            if (isFFAMode.get()) {
                kitProvider.applyKit(online, "runner");
            } else if (runners.contains(online.getUniqueId())) {
                kitProvider.applyKit(online, "runner");
            } else {
                kitProvider.applyKit(online, "hunter");
            }

            platform.spawnFireworks(online.getLocation());
        }
        
        if (platform.getConfigBoolean("grace-period.enabled", true)) startGracePeriod();
    }

    private void startGracePeriod() {
        isGracePeriod.set(true);
        int duration = platform.getConfigInt("grace-period.duration", 60);
        messageManager.broadcast("grace_period_start", duration);
        cancelTask(graceTask);
        final AtomicInteger remaining = new AtomicInteger(duration);
        graceTask = platform.getScheduler().runTaskTimer(() -> {
            int current = remaining.get();
            if (current <= 0) {
                isGracePeriod.set(false);
                messageManager.broadcast("grace_period_end");
                cancelTask(graceTask);
                graceTask = null;
            } else {
                if (current <= 5 || current % 10 == 0) messageManager.broadcast("grace_period_countdown", current);
                remaining.decrementAndGet();
            }
        }, 0L, 20L);
    }

    public void stopTasks() {
        cancelTask(countdownTask); countdownTask = null;
        cancelTask(graceTask); graceTask = null;
        cancelTask(disconnectTask); disconnectTask = null;
        cancelTask(borderTask); borderTask = null;
    }

    private void cancelTask(MScheduler.Task task) {
        if (task != null) task.cancel();
    }

    public void endGame(@Nullable MLocation finishLoc, boolean hunterWin) {
        isGameRunning.set(false);
        stopTasks();
        
        for (MWorld world : platform.getWorlds()) platform.resetWorldBorder(world.getName());

        String titleKey = hunterWin ? "hunter_winner_title" : "runner_winner_title";
        for (MPlayer online : platform.getOnlinePlayers()) {
            if (finishLoc != null) online.teleport(finishLoc);
            messageManager.sendTitle(online, titleKey, 10, 40, 10);
            
            platform.spawnFireworks(online.getLocation());
        }

        if (finishLoc != null) {
            for (int i = 0; i < 5; i++) {
                final int delay = i * 20;
                platform.getScheduler().runTaskLater(() -> platform.spawnFireworks(finishLoc), (long) delay);
            }
        }
    }

    public boolean isHunter(@NotNull MPlayer player) {
        return !isFFAMode.get() && !runners.contains(player.getUniqueId());
    }

    public boolean isRunner(@NotNull MPlayer player) {
        return isFFAMode.get() || runners.contains(player.getUniqueId());
    }

    @Nullable 
    public MPlayer getFirstRunner() {
        if (runners.isEmpty()) return null;
        return platform.getPlayer(runners.iterator().next());
    }

    public void surroundRunner(@NotNull MPlayer runner) {
        MLocation center = runner.getLocation();
        double radius = 5.0;
        
        java.util.List<MPlayer> hunters = new java.util.ArrayList<>();
        for (MPlayer p : platform.getOnlinePlayers()) {
            if (isHunter(p) && !deadHunters.contains(p.getUniqueId())) {
                hunters.add(p);
            }
        }

        int n = hunters.size();
        if (n == 0) return;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);

            MLocation newLoc = new MLocation(
                center.getWorldName(),
                center.getX() + xOffset,
                center.getY(),
                center.getZ() + zOffset,
                (float) Math.toDegrees(angle + Math.PI), // Face center
                0
            );
            hunters.get(i).teleport(newLoc);
        }
    }

    // Getters and Setters
    public Set<UUID> getRunners() { return runners; }
    public void addRunner(UUID uuid) { runners.add(uuid); }
    public void removeRunner(UUID uuid) { runners.remove(uuid); }
    public void clearRunners() { runners.clear(); }
    public boolean isGameRunning() { return isGameRunning.get(); }
    public boolean isHardcore() { return isHardcore.get(); }
    public void setHardcore(boolean hardcore) { isHardcore.set(hardcore); }
    public boolean isFFAMode() { return isFFAMode.get(); }
    public void setFFAMode(boolean FFAMode) { isFFAMode.set(FFAMode); }
    public boolean isGracePeriod() { return isGracePeriod.get(); }
    public boolean isDisconnectCountdown() { return isDisconnectCountdown.get(); }
    public boolean isAdvancementAnnouncementsEnabled() { return advancementAnnouncements.get(); }
    public void setAdvancementAnnouncementsEnabled(boolean enabled) { advancementAnnouncements.set(enabled); }
    public long getGameStartTime() { return gameStartTime.get(); }
    public Set<UUID> getDeadHunters() { return deadHunters; }

    public boolean isSmokeBombEnabled() { return isSmokeBombEnabled.get(); }
    public void setSmokeBombEnabled(boolean enabled) { isSmokeBombEnabled.set(enabled); }
    public int getBorderType() { return borderType.get(); }
    public void setBorderType(int type) { borderType.set(type); }
    public int getBorderSize() { return borderSize.get(); }
    public void setBorderSize(int size) { borderSize.set(size); }
    public Map<UUID, Long> getAbilityCooldowns() { return abilityCooldowns; }
}
