package com.example.manhunt.manager;

import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MScheduler;
import com.example.manhunt.message.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompassManager {

    private final MPlatform platform;
    private final GameManager gameManager;
    private final MessageManager messageManager;

    private volatile UUID runnerTrackTarget;
    private volatile MLocation lastPortalLocation;
    private final AtomicBoolean isTrackingActive = new AtomicBoolean(false);

    private final Map<UUID, Integer> compassModes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> ffaTargets = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> hunterRunnerTargets = new ConcurrentHashMap<>();

    private volatile MScheduler.Task compassTask;
    private volatile MScheduler.Task trackingCountdownTask;

    public CompassManager(MPlatform platform, GameManager gameManager, MessageManager messageManager) {
        this.platform = platform;
        this.gameManager = gameManager;
        this.messageManager = messageManager;
    }

    public void startUpdateTask() {
        cancelTask(compassTask);
        compassTask = platform.getScheduler().runTaskTimer(this::updateAllCompassTargets, 20L, 20L);
    }

    public void stopTasks() {
        cancelTask(compassTask); compassTask = null;
        cancelTask(trackingCountdownTask); trackingCountdownTask = null;
    }

    private void cancelTask(MScheduler.Task task) {
        if (task != null) task.cancel();
    }

    public void startTrackingDelay() {
        isTrackingActive.set(false);
        int delay = platform.getConfigInt("tracking-delay", 600);
        cancelTask(trackingCountdownTask);
        trackingCountdownTask = platform.getScheduler().runTaskLater(() -> {
            isTrackingActive.set(true);
            messageManager.broadcast("tracking_active");
            trackingCountdownTask = null;
        }, 20L * delay);
    }

    public void updateAllCompassTargets() {
        for (MPlayer p : platform.getOnlinePlayers()) {
            int mode = compassModes.getOrDefault(p.getUniqueId(), 0);
            MLocation targetLoc = null;

            if (gameManager.isFFAMode()) {
                UUID targetUUID = ffaTargets.get(p.getUniqueId());
                if (targetUUID != null) {
                    MPlayer target = platform.getPlayer(targetUUID);
                    if (target != null && target.isOnline() && target.getLocation().getWorldName().equals(p.getLocation().getWorldName())) {
                        targetLoc = target.getLocation();
                    }
                }
            } else if (gameManager.isRunner(p)) {
                if (runnerTrackTarget != null) {
                    MPlayer target = platform.getPlayer(runnerTrackTarget);
                    if (target != null && target.isOnline() && target.getLocation().getWorldName().equals(p.getLocation().getWorldName())) {
                        targetLoc = target.getLocation();
                    }
                }
            } else if (gameManager.isHunter(p)) {
                if (mode == 0) { // Runner
                    UUID targetUUID = hunterRunnerTargets.get(p.getUniqueId());
                    MPlayer runner = targetUUID != null ? platform.getPlayer(targetUUID) : gameManager.getFirstRunner();

                    if (isTrackingActive.get() && runner != null && runner.isOnline() && runner.getLocation().getWorldName().equals(p.getLocation().getWorldName())) {
                        targetLoc = runner.getLocation();
                    } else if (lastPortalLocation != null && lastPortalLocation.getWorldName().equals(p.getLocation().getWorldName())) {
                        targetLoc = lastPortalLocation;
                    }
                } else if (mode == 1) { // Teammate
                    targetLoc = findNearestTeammate(p);
                } else if (mode == 2) { // Portal
                    if (lastPortalLocation != null && lastPortalLocation.getWorldName().equals(p.getLocation().getWorldName())) {
                        targetLoc = lastPortalLocation;
                    }
                }
            }
            
            if (targetLoc == null) {
                com.example.manhunt.api.MWorld world = platform.getWorld(p.getLocation().getWorldName());
                if (world != null) targetLoc = world.getSpawnLocation();
            }
            
            if (targetLoc != null) {
                p.updateInventoryCompass(targetLoc);
            }
        }
    }

    private MLocation findNearestTeammate(MPlayer player) {
        MPlayer nearest = null;
        double minDist = Double.MAX_VALUE;
        for (MPlayer other : platform.getOnlinePlayers()) {
            if (other.getUniqueId().equals(player.getUniqueId()) || !gameManager.isHunter(other) || !other.getLocation().getWorldName().equals(player.getLocation().getWorldName())) continue;
            double dist = other.getLocation().distanceSquared(player.getLocation());
            if (dist < minDist) { minDist = dist; nearest = other; }
        }
        if (nearest != null) return nearest.getLocation();
        
        com.example.manhunt.api.MWorld world = platform.getWorld(player.getLocation().getWorldName());
        return world != null ? world.getSpawnLocation() : null;
    }

    public void cycleCompassMode(MPlayer player) {
        if (gameManager.isFFAMode()) {
            List<MPlayer> targets = new ArrayList<>(platform.getOnlinePlayers());
            targets.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
            
            if (targets.isEmpty()) return;

            UUID currentTarget = ffaTargets.get(player.getUniqueId());
            int currentIndex = -1;
            if (currentTarget != null) {
                for (int i = 0; i < targets.size(); i++) {
                    if (targets.get(i).getUniqueId().equals(currentTarget)) {
                        currentIndex = i;
                        break;
                    }
                }
            }

            int nextIndex = (currentIndex + 1) % targets.size();
            MPlayer nextTarget = targets.get(nextIndex);
            ffaTargets.put(player.getUniqueId(), nextTarget.getUniqueId());
            
            messageManager.sendMessage(player, "ffa_tracking_target", nextTarget.getName());
            updateAllCompassTargets();
            return;
        }

        int current = compassModes.getOrDefault(player.getUniqueId(), 0);

        // If in runner mode and there are multiple runners, cycle through runners first
        if (current == 0 && gameManager.getRunners().size() > 1) {
            List<UUID> runnerList = new ArrayList<>(gameManager.getRunners());
            UUID currentTarget = hunterRunnerTargets.get(player.getUniqueId());
            int currentIndex = runnerList.indexOf(currentTarget);

            if (currentIndex < runnerList.size() - 1) {
                UUID nextRunner = runnerList.get(currentIndex + 1);
                hunterRunnerTargets.put(player.getUniqueId(), nextRunner);
                MPlayer nextPlayer = platform.getPlayer(nextRunner);
                if (nextPlayer != null) {
                    messageManager.sendMessage(player, "compass_tracking_runner", nextPlayer.getName());
                }
                updateAllCompassTargets();
                return;
            } else {
                // Reset runner target to first one and move to next mode
                hunterRunnerTargets.put(player.getUniqueId(), runnerList.get(0));
            }
        }

        int next = (current + 1) % 3;
        compassModes.put(player.getUniqueId(), next);
        String modeName = "compass_mode_runner";
        if (next == 1) modeName = "compass_mode_teammate";
        else if (next == 2) modeName = "compass_mode_portal";
        messageManager.sendMessage(player, "compass_mode_change", messageManager.getRawMessage(player, modeName));
        updateAllCompassTargets();
    }

    public void cleanupPlayer(UUID uuid) {
        compassModes.remove(uuid);
        ffaTargets.remove(uuid);
        hunterRunnerTargets.remove(uuid);
    }

    public UUID getRunnerTrackTarget() { return runnerTrackTarget; }
    public void setRunnerTrackTarget(UUID runnerTrackTarget) { this.runnerTrackTarget = runnerTrackTarget; }
    public MLocation getLastPortalLocation() { return lastPortalLocation; }
    public void setLastPortalLocation(MLocation lastPortalLocation) { this.lastPortalLocation = lastPortalLocation; }
    public boolean isTrackingActive() { return isTrackingActive.get(); }
    public Map<UUID, Integer> getCompassModes() { return compassModes; }
}
