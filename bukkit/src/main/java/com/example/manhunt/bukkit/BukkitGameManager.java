package com.example.manhunt.bukkit;

import com.example.manhunt.Main;
import com.example.manhunt.api.MLocation;
import com.example.manhunt.bukkit.impl.BukkitPlayer;
import com.example.manhunt.manager.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitGameManager extends GameManager {

    private final Main plugin;

    public BukkitGameManager(Main plugin) {
        super(plugin.getPlatform(), plugin.getMessageManager(), plugin.getKitManager());
        this.plugin = plugin;
    }

    @Override
    public void endGame(MLocation finishLoc, boolean hunterWin) {
        super.endGame(finishLoc, hunterWin);
        // Add Bukkit specific effects like fireworks here if needed
    }

    public boolean isHunter(@NotNull Player player) {
        return super.isHunter(new BukkitPlayer(player));
    }

    public boolean isRunner(@NotNull Player player) {
        return super.isRunner(new BukkitPlayer(player));
    }

    public Player getBukkitFirstRunner() {
        com.example.manhunt.api.MPlayer runner = super.getFirstRunner();
        if (runner instanceof BukkitPlayer) {
            return ((BukkitPlayer) runner).getBukkitPlayer();
        }
        return null;
    }

    // Event handling bridge (called from ManhuntListener)
    public void handleDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (isRunner(victim)) {
            Location loc = victim.getLocation();
            endGame(new MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()), true);
        } else if (isHunter(victim)) {
            if (isHardcore()) {
                victim.setGameMode(org.bukkit.GameMode.SPECTATOR);
                victim.getInventory().clear();
                victim.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.COMPASS));
                plugin.getMessageManager().broadcast("hunter_became_spectator", victim.getName());
            }
        }
    }

    public void handleRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isHunter(player)) {
            plugin.getKitManager().applyKit(new BukkitPlayer(player), "hunter");
            plugin.getScenarioManager().applyClassEffects(new BukkitPlayer(player));
            
            Player runner = getBukkitFirstRunner();
            if (runner != null && runner.isOnline() && runner.getWorld().equals(player.getWorld())) {
                int min = plugin.getConfig().getInt("hunter.respawn-distance.min", 50);
                int max = plugin.getConfig().getInt("hunter.respawn-distance.max", 100);
                int dist = min + new java.util.Random().nextInt(max - min);
                
                double angle = new java.util.Random().nextDouble() * 2 * Math.PI;
                double x = runner.getLocation().getX() + Math.cos(angle) * dist;
                double z = runner.getLocation().getZ() + Math.sin(angle) * dist;
                double y = runner.getWorld().getHighestBlockYAt((int)x, (int)z) + 1;
                
                event.setRespawnLocation(new Location(runner.getWorld(), x, y, z));
            }
        }
    }
}
