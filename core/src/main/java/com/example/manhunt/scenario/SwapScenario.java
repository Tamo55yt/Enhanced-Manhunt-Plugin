package com.example.manhunt.scenario;

import com.example.manhunt.api.AbstractScenario;
import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.manager.GameManager;
import com.example.manhunt.message.MessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwapScenario extends AbstractScenario {
    private final MPlatform platform;
    private final GameManager gameManager;
    private final MessageManager messageManager;
    private int tickCounter = 0;

    public SwapScenario(MPlatform platform, GameManager gameManager, MessageManager messageManager) {
        super("SWAP", "Yer Değiştirme");
        this.platform = platform;
        this.gameManager = gameManager;
        this.messageManager = messageManager;
    }

    @Override
    public void onStart() {
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (tickCounter % 1200 != 0) return; // Every 1200 ticks (1 minute)

        List<MPlayer> participants = new ArrayList<>();
        for (MPlayer p : platform.getOnlinePlayers()) {
            if (gameManager.isHunter(p) || gameManager.isRunner(p)) {
                participants.add(p);
            }
        }

        if (participants.size() < 2) return;
        
        messageManager.broadcastRaw("&d&l[SWAP] &5Oyuncuların yerleri değişiyor!");
        List<MLocation> locs = new ArrayList<>();
        for (MPlayer p : participants) locs.add(p.getLocation());
        Collections.shuffle(locs);
        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).teleport(locs.get(i));
        }
    }
}
