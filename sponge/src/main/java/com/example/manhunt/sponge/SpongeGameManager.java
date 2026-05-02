package com.example.manhunt.sponge;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.manager.GameManager;
import com.example.manhunt.sponge.impl.SpongePlayer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Random;

public class SpongeGameManager extends GameManager {

    private final SpongeMain plugin;
    private final Random random = new Random();

    public SpongeGameManager(SpongeMain plugin) {
        super(plugin.getPlatform(), plugin.getMessageManager(), new SpongeKitManager(plugin));
        this.plugin = plugin;
    }

    @Override
    public void startGame() {
        super.startGame();
        plugin.getScoreboardManager().startScoreboardTask();
    }

    @Override
    public void endGame(MLocation finishLoc, boolean hunterWin) {
        super.endGame(finishLoc, hunterWin);
        plugin.getScoreboardManager().stopTasks();
    }

    public void handleDeath(ServerPlayer player) {
        if (isRunner(new SpongePlayer(player))) {
            ServerLocation loc = player.serverLocation();
            endGame(new MLocation(loc.world().key().toString(), loc.x(), loc.y(), loc.z(), 0, 0), true);
        } else if (isHunter(new SpongePlayer(player))) {
            if (isHardcore()) {
                new SpongePlayer(player).setGameMode("spectator");
                player.inventory().clear();
                new SpongePlayer(player).giveItem("minecraft:compass", 1);
                plugin.getMessageManager().broadcast("hunter_became_spectator", player.name());
            }
        }
    }

    public void handleRespawn(org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent event) {
        ServerPlayer player = event.entity();
        if (isHunter(new SpongePlayer(player))) {
            // Apply kit and effects
            new SpongeKitManager(plugin).applyKit(new SpongePlayer(player), "hunter");
            plugin.getScenarioManager().applyClassEffects(new SpongePlayer(player));

            MPlayer runner = getFirstRunner();
            if (runner != null && runner.isOnline()) {
                MLocation rLoc = runner.getLocation();
                if (player.world().key().toString().equals(rLoc.getWorldName())) {
                    int min = plugin.getPlatform().getConfigInt("hunter.respawn-distance.min", 50);
                    int max = plugin.getPlatform().getConfigInt("hunter.respawn-distance.max", 100);
                    int dist = min + random.nextInt(max - min);

                    double angle = random.nextDouble() * 2 * Math.PI;
                    double x = rLoc.getX() + Math.cos(angle) * dist;
                    double z = rLoc.getZ() + Math.sin(angle) * dist;
                    
                    ServerWorld world = player.world();
                    // In Sponge 8+, use height(HeightType, int, int)
                    int y = world.height(HeightTypes.WORLD_SURFACE.get(), (int)x, (int)z);
                    
                    player.setLocation(ServerLocation.of(world, x, y + 1, z));
                }
            }
        }
    }
}
