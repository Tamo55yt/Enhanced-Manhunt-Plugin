package com.example.manhunt.scenario;

import com.example.manhunt.api.AbstractScenario;
import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class GravityShiftScenario extends AbstractScenario {
    private final MPlatform platform;
    private final AtomicInteger tickCounter = new AtomicInteger(0);
    private int currentGravityMode = 0; // 0: Normal, 1: Low, 2: High, 3: Zero (Simulated)

    public GravityShiftScenario(MPlatform platform) {
        super("GRAVITY_SHIFT", "Yerçekimi Değişimi");
        this.platform = platform;
    }

    @Override
    public void onTick() {
        if (!active) return;
        
        // Every 5 minutes (5 * 60 * 20 = 6000 ticks)
        if (tickCounter.incrementAndGet() >= 6000) {
            tickCounter.set(0);
            currentGravityMode = (currentGravityMode + 1) % 4;
            applyGravity();
        }
    }

    private void applyGravity() {
        String msg = "§6[SENARYO] §eYerçekimi değişti: ";
        switch (currentGravityMode) {
            case 0: msg += "Normal"; break;
            case 1: msg += "Düşük (Ay)"; break;
            case 2: msg += "Yüksek"; break;
            case 3: msg += "Sıfır Yerçekimi (Uçma)"; break;
        }
        platform.broadcast(msg);
        
        for (MPlayer player : platform.getOnlinePlayers()) {
            updatePlayerGravity(player);
        }
    }

    public void updatePlayerGravity(MPlayer player) {
        if (!active) return;
        player.removePotionEffect("minecraft:jump_boost");
        player.removePotionEffect("minecraft:slow_falling");
        
        switch (currentGravityMode) {
            case 1: // Low
                player.addPotionEffect("minecraft:jump_boost", 7000, 2);
                player.addPotionEffect("minecraft:slow_falling", 7000, 1);
                break;
            case 2: // High
                player.addPotionEffect("minecraft:jump_boost", 7000, -2); // Reduced jumping
                break;
            case 3: // Zero (Fly mode)
                player.setAllowFlight(true);
                break;
            default:
                player.setAllowFlight(false);
                break;
        }
    }

    @Override
    public void onStop() {
        for (MPlayer player : platform.getOnlinePlayers()) {
            player.removePotionEffect("minecraft:jump_boost");
            player.removePotionEffect("minecraft:slow_falling");
            player.setAllowFlight(false);
        }
    }
}
