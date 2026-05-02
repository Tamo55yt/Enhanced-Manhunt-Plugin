package com.example.manhunt.sponge;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.manager.CompassManager;

public class SpongeCompassManager extends CompassManager {

    public SpongeCompassManager(SpongeMain plugin) {
        super(plugin.getPlatform(), plugin.getGameManager(), plugin.getMessageManager());
    }

    public void giveCompass(MPlayer player) {
        player.giveItem("minecraft:compass", 1);
        getCompassModes().putIfAbsent(player.getUniqueId(), 0);
    }
}
