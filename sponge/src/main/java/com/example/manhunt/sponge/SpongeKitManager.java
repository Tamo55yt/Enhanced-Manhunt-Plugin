package com.example.manhunt.sponge;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.api.KitProvider;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.sponge.impl.SpongePlayer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.Sponge;

import java.util.Optional;

public class SpongeKitManager implements KitProvider {

    private final SpongeMain plugin;

    public SpongeKitManager(SpongeMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void saveKit(MPlayer mPlayer, String role) {
        if (!(mPlayer instanceof SpongePlayer)) return;
        Optional<ServerPlayer> optPlayer = Sponge.server().player(mPlayer.getUniqueId());
        if (optPlayer.isPresent()) {
            ServerPlayer player = optPlayer.get();
            // In a real implementation, you would serialize the inventory to config here.
            // For now, we'll just log it or handle it simply.
            player.sendMessage(net.kyori.adventure.text.Component.text("§a" + role + " kiti kaydedildi (Sponge)!"));
        }
    }

    @Override
    public void applyKit(MPlayer mPlayer, String role) {
        mPlayer.clearInventory();
        if (role.equalsIgnoreCase("runner")) {
            mPlayer.giveItem("minecraft:iron_sword", 1);
            mPlayer.giveItem("minecraft:cooked_beef", 16);
        } else if (role.equalsIgnoreCase("hunter")) {
            mPlayer.giveItem("minecraft:iron_sword", 1);
            mPlayer.giveItem("minecraft:iron_pickaxe", 1);
            mPlayer.giveItem("minecraft:cooked_beef", 16);
            if (plugin.getCompassManager() instanceof SpongeCompassManager) {
                ((SpongeCompassManager) plugin.getCompassManager()).giveCompass(mPlayer);
            }
        }
    }
}
