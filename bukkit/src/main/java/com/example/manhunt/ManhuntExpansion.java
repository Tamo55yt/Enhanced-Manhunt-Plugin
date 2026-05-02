package com.example.manhunt;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ManhuntExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public ManhuntExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "manhunt";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Example";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %manhunt_status%
        if (params.equalsIgnoreCase("status")) {
            return plugin.getGameManager().isGameRunning() ? "ACTIVE" : "INACTIVE";
        }

        return null;
    }
}
