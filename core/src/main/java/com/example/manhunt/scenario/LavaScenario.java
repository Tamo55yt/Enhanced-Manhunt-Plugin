package com.example.manhunt.scenario;

import com.example.manhunt.api.AbstractScenario;
import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MWorld;
import com.example.manhunt.message.MessageManager;

public class LavaScenario extends AbstractScenario {
    private final MPlatform platform;
    private final MessageManager messageManager;
    private int lavaY = 0;
    private int tickCounter = 0;

    public LavaScenario(MPlatform platform, MessageManager messageManager) {
        super("LAVA", "Yükselen Lav");
        this.platform = platform;
        this.messageManager = messageManager;
    }

    @Override
    public void onStart() {
        lavaY = 0;
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (tickCounter % 120 != 0) return; // Every 120 ticks (6 seconds)

        for (MWorld world : platform.getWorlds()) {
            if (lavaY >= world.getMaxHeight()) return;

            messageManager.broadcastRaw("&c&l[LAVA] &6Lav seviyesi yükseliyor! Mevcut: " + lavaY);
            for (int x = -100; x <= 100; x++) {
                for (int z = -100; z <= 100; z++) {
                    world.setBlock(x, lavaY, z, "LAVA");
                }
            }
        }
        lavaY++;
    }
}
