package com.example.manhunt.bukkit.impl;

import com.example.manhunt.api.MPlatform;
import com.example.manhunt.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitMessageManager extends MessageManager {

    public BukkitMessageManager(@NotNull MPlatform platform) {
        super(platform);
    }

    public void sendMessage(@NotNull Player player, @NotNull String key, Object... args) {
        super.sendMessage(new BukkitPlayer(player), key, args);
    }

    public void sendTitle(@NotNull Player player, @NotNull String key, int fadeIn, int stay, int fadeOut) {
        super.sendTitle(new BukkitPlayer(player), key, fadeIn, stay, fadeOut);
    }

    public void sendActionBar(@NotNull Player player, @NotNull String key, Object... args) {
        super.sendActionBar(new BukkitPlayer(player), key, args);
    }

    @NotNull
    public String getRawMessage(@Nullable Player player, @NotNull String key) {
        return super.getRawMessage(player == null ? null : new BukkitPlayer(player), key);
    }

    public void broadcastRaw(@NotNull String rawMessage) {
        Bukkit.broadcastMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', rawMessage));
    }
}
